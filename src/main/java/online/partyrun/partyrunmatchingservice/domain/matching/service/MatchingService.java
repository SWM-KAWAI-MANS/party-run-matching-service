package online.partyrun.partyrunmatchingservice.domain.matching.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MatchingService {
    MatchingRepository matchingRepository;
    MatchingSinkHandler matchingSinkHandler;

    public Mono<Matching> create(final List<String> memberIds, final int distance) {
        final List<MatchingMember> members = memberIds.stream().map(MatchingMember::new).toList();
        disconnectLeftMember(memberIds);
        return saveMatchAndSendEvents(members, distance);
    }

    private Mono<Matching> saveMatchAndSendEvents(List<MatchingMember> members, int distance) {
        return matchingRepository
                .save(new Matching(members, distance))
                .doOnSuccess(
                        match ->
                                members.forEach(
                                        member -> {
                                            matchingSinkHandler.create(member.getId());
                                            matchingSinkHandler.sendEvent(
                                                    member.getId(), new MatchEvent(match));
                                        }));
    }

    private void disconnectLeftMember(List<String> memberIds) {
        memberIds.forEach(matchingSinkHandler::disconnectIfExist);
        matchingRepository
                .findAllByMembersIdInAndMembersStatus(memberIds, MatchingMemberStatus.NO_RESPONSE)
                .flatMap(matching -> {
                    matching.cancel();
                    return matchingRepository.save(matching);
                }).blockLast();
    }
}
