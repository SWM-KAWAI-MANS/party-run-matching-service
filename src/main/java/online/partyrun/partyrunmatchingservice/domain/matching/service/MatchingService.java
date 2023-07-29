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

import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;

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
        disconnectLeftMember(memberIds);
        return saveMatchAndSendEvents(members, distance);
    }

    private void disconnectLeftMember(List<String> memberIds) {
        memberIds.forEach(matchingSinkHandler::disconnectIfExist);
        matchingRepository
                .findAllByMembersIdInAndMembersStatus(memberIds, MatchingMemberStatus.NO_RESPONSE)
                .flatMap(toCancelAndSave())
                .subscribe();
    }

    private Function<Matching, Publisher<? extends Matching>> toCancelAndSave() {
        return matching -> {
            matching.cancel();
            return matchingRepository.save(matching);
        };
    }

    private Mono<Matching> saveMatchAndSendEvents(List<MatchingMember> members, int distance) {
        return matchingRepository
                .save(new Matching(members, distance))
                .doOnSuccess(
                        match ->
                                members.forEach(member ->
                                        createSink(match, member)));
    }

    private void createSink(final Matching match, final MatchingMember member) {
        matchingSinkHandler.create(member.getId());
        matchingSinkHandler.sendEvent(
                member.getId(), new MatchEvent(match));
    }

    public Mono<Matching> setMemberStatus(
            final Mono<String> member, final MatchingRequest request) {
        return member.flatMap(memberId -> updateMatchMemberStatus(request, memberId))
                .flatMap(this::updateMatchStatus)
                .doOnSuccess(this::multiCastEvent);
    }

    private Mono<Matching> updateMatchMemberStatus(final MatchingRequest request, final String memberId) {
        return findMatchingByNoResponseMember(memberId)
                .flatMap(toUpdateMatching(request.isJoin(), memberId));
    }

    private Mono<Matching> findMatchingByNoResponseMember(final String memberId) {
        return matchingRepository
                .findByMembersIdAndMembersStatus(
                        memberId, MatchingMemberStatus.NO_RESPONSE);
    }

    private Function<Matching, Mono<? extends Matching>> toUpdateMatching(final boolean isJoin, final String memberId) {
        return matching -> findByMemberIdAndUpdate(isJoin, memberId, matching.getId());
    }
    private Mono<Matching> findByMemberIdAndUpdate(final boolean isJoin, final String memberId, final String matchingId) {
        return matchingRepository
                .updateMatchingMemberStatus(
                        matchingId,
                        memberId,
                        MatchingMemberStatus
                                .getByIsJoin(isJoin))
                .then(Mono.defer(
                        () -> matchingRepository.findById(matchingId)));
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
}
