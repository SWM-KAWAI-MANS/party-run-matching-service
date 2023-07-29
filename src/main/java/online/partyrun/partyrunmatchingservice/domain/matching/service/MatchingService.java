package online.partyrun.partyrunmatchingservice.domain.matching.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.matching.controller.MatchingRequest;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MatchingService {
    MatchingRepository matchingRepository;
    MatchingSinkHandler matchingSinkHandler;

    public Mono<Matching> create(final List<String> memberIds, final int distance) {
        final List<MatchingMember> members = memberIds.stream().map(MatchingMember::new).toList();
        return disconnectLeftMember(memberIds)
                .then(saveMatchAndSendEvents(members, distance));
    }

    private Mono<Void> disconnectLeftMember(List<String> memberIds) {
        memberIds.forEach(matchingSinkHandler::disconnectIfExist);
        return matchingRepository
                .findAllByMembersIdInAndMembersStatus(memberIds, MatchingMemberStatus.NO_RESPONSE)
                .doOnNext(Matching::cancel)
                .flatMap(matchingRepository::save)
                .then();
    }


    private Mono<Matching> saveMatchAndSendEvents(List<MatchingMember> members, int distance) {
        return matchingRepository
                .save(new Matching(members, distance))
                .doOnSuccess(match -> members.forEach(member -> createSink(match, member)));
    }

    private void createSink(final Matching match, final MatchingMember member) {
        matchingSinkHandler.create(member.getId());
        matchingSinkHandler.sendEvent(member.getId(), new MatchEvent(match));
    }

    public Mono<Matching> setMemberStatus(
            final Mono<String> member, final MatchingRequest request) {
        return member.flatMap(memberId -> updateMatchingMemberStatus(request, memberId))
                .flatMap(this::updateMatchStatus)
                .doOnSuccess(this::multiCastEvent);
    }

    private Mono<Matching> updateMatchingMemberStatus(final MatchingRequest request, final String memberId) {
        return findMatchingByNoResponseMember(memberId)
                .flatMap(matching ->
                        updateMatching(request.isJoin(), memberId, matching.getId()));
    }

    private Mono<Matching> findMatchingByNoResponseMember(final String memberId) {
        return matchingRepository.findByMembersIdAndMembersStatus(
                memberId, MatchingMemberStatus.NO_RESPONSE);
    }

    private Mono<Matching> updateMatching(final boolean isJoin, final String memberId, final String matchingId) {
        return matchingRepository.updateMatchingMemberStatus(matchingId, memberId, MatchingMemberStatus.getByIsJoin(isJoin))
                .then(Mono.defer(() -> matchingRepository.findById(matchingId)));
    }

    private Mono<Matching> updateMatchStatus(final Matching match) {
        match.updateStatus();
        if (!match.isWait()) {
            return matchingRepository.save(match);
        }
        return Mono.just(match);
    }

    private void multiCastEvent(final Matching matching) {
        matching.getMembers()
                .forEach(
                        member ->
                                matchingSinkHandler.sendEvent(
                                        member.getId(), new MatchEvent(matching)));
    }

    public Flux<MatchEvent> getEventSteam(final Mono<String> member) {
        return member.flatMapMany(
                id ->
                        matchingSinkHandler
                                .connect(id)
                                .doOnNext(
                                        event -> {
                                            if (!event.status().isWait()) {
                                                matchingSinkHandler.complete(id);
                                            }
                                        }));
    }
}
