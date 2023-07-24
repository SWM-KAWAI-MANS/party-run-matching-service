package online.partyrun.partyrunmatchingservice.domain.matching.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;
import online.partyrun.partyrunmatchingservice.global.sse.ServerSentEventHandler;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MatchingService {
    MatchingRepository matchingRepository;
    ServerSentEventHandler<String, MatchEvent> matchEventHandler;

    public Mono<Matching> create(final List<String> memberIds, final int distance) {
        final List<MatchingMember> members = memberIds.stream().map(MatchingMember::new).toList();
        memberIds.forEach(this::disconnectLeftMember);

        return saveMatchAndSendEvents(members, distance);
    }

    private Mono<Matching> saveMatchAndSendEvents(List<MatchingMember> members, int distance) {
        return matchingRepository
                .save(new Matching(members, distance))
                .doOnSuccess(
                        match ->
                                members.forEach(
                                        member -> {
                                            matchEventHandler.create(member.getId());
                                            matchEventHandler.sendEvent(
                                                    member.getId(), new MatchEvent(match));
                                        }));
    }

    private void disconnectLeftMember(String memberId) {
        matchingRepository
                .findByMembersIdAndMembersStatus(memberId, MatchingMemberStatus.NO_RESPONSE)
                .flatMap(
                        matching -> {
                            matching.updateMemberStatus(memberId, false);
                            matchEventHandler.complete(memberId);
                            return matchingRepository.save(matching);
                        })
                .block();
    }
}
