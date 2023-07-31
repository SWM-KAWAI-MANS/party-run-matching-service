package online.partyrun.partyrunmatchingservice.domain.matching.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.matching.controller.MatchingRequest;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;

import org.reactivestreams.Publisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MatchingService {
    private static final int REMOVE_SINK_SCHEDULE_TIME = 3_600_000; // 3시간 마다 실행
    MatchingRepository matchingRepository;
    MatchingSinkHandler matchingSinkHandler;
    Clock clock;

    public Mono<Matching> create(final List<String> memberIds, final int distance) {
        final List<MatchingMember> members = memberIds.stream().map(MatchingMember::new).toList();
        disconnectLeftMember(memberIds);
        return saveMatchAndSendEvents(members, distance);
    }

    private void disconnectLeftMember(List<String> memberIds) {
        memberIds.forEach(matchingSinkHandler::disconnectIfExist);
        matchingRepository
                .findAllByMembersIdInAndMembersStatus(memberIds, MatchingMemberStatus.NO_RESPONSE)
                .flatMap(cancelMatching())
                .subscribe();
    }

    private Function<Matching, Publisher<Matching>> cancelMatching() {
        return matching -> {
            matching.cancel();
            return matchingRepository.save(matching);
        };
    }

    private Mono<Matching> saveMatchAndSendEvents(List<MatchingMember> members, int distance) {
        return matchingRepository
                .save(new Matching(members, distance, LocalDateTime.now(clock)))
                .doOnSuccess(match -> members.forEach(member -> createSink(match, member)));
    }

    private void createSink(final Matching match, final MatchingMember member) {
        matchingSinkHandler.create(member.getId());
        matchingSinkHandler.sendEvent(member.getId(), new MatchEvent(match));
    }

    public Mono<Matching> setMemberStatus(
            final Mono<String> member, final MatchingRequest request) {
        return member.flatMap(memberId -> updateMatchMemberStatus(request, memberId))
                .flatMap(this::updateMatchStatus)
                .doOnSuccess(this::multiCastEvent);
    }

    private Mono<Matching> updateMatchMemberStatus(
            final MatchingRequest request, final String memberId) {
        return findMatchingByNoResponseMember(memberId)
                .flatMap(updateMatching(request.isJoin(), memberId));
    }

    private Mono<Matching> findMatchingByNoResponseMember(final String memberId) {
        return matchingRepository.findByMembersIdAndMembersStatus(
                memberId, MatchingMemberStatus.NO_RESPONSE);
    }

    private Function<Matching, Mono<Matching>> updateMatching(
            final boolean isJoin, final String memberId) {
        return matching -> findByMemberIdAndUpdate(isJoin, memberId, matching.getId());
    }

    private Mono<Matching> findByMemberIdAndUpdate(
            final boolean isJoin, final String memberId, final String matchingId) {
        return matchingRepository
                .updateMatchingMemberStatus(
                        matchingId, memberId, MatchingMemberStatus.getByIsJoin(isJoin))
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

    @Scheduled(fixedDelay = REMOVE_SINK_SCHEDULE_TIME)
    public void removeUnConnectedSink() {
        matchingRepository
                .findAllByStatus(MatchingStatus.WAIT)
                .filter(matching -> matching.isTimeOut(LocalDateTime.now(clock)))
                .doOnNext(Matching::cancel)
                .doOnNext(this::disconnectAllMember)
                .flatMap(matchingRepository::save)
                .subscribe();
    }

    private void disconnectAllMember(Matching matching) {
        matching.getMembers()
                .forEach(member -> matchingSinkHandler.disconnectIfExist(member.getId()));
    }
}
