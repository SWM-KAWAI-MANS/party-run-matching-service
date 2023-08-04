package online.partyrun.partyrunmatchingservice.domain.matching.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import online.partyrun.partyrunmatchingservice.domain.battle.BattleService;
import online.partyrun.partyrunmatchingservice.domain.matching.controller.MatchingRequest;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MatchingService {
    private static final int REMOVE_SINK_SCHEDULE_TIME = 3_600_000; // 3시간 마다 실행
    MatchingRepository matchingRepository;
    MatchingSinkHandler matchingSinkHandler;
    BattleService battleService;
    Clock clock;

    public Mono<Matching> create(final List<String> memberIds, final int distance) {
        final List<MatchingMember> members = memberIds.stream().map(MatchingMember::new).toList();
        return disconnectLeftMember(memberIds).then(saveMatchAndSendEvents(members, distance));
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
                .save(new Matching(members, distance, LocalDateTime.now(clock)))
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

    private Mono<Matching> updateMatchingMemberStatus(
            final MatchingRequest request, final String memberId) {
        return findMatchingByNoResponseMember(memberId)
                .flatMap(matching -> updateMatching(request.isJoin(), memberId, matching.getId()));
    }

    private Mono<Matching> findMatchingByNoResponseMember(final String memberId) {
        return matchingRepository.findByMembersIdAndMembersStatus(
                memberId, MatchingMemberStatus.NO_RESPONSE);
    }

    private Mono<Matching> updateMatching(
            final boolean isJoin, final String memberId, final String matchingId) {
        return matchingRepository
                .updateMatchingMemberStatus(
                        matchingId, memberId, MatchingMemberStatus.getByIsJoin(isJoin))
                .then(Mono.defer(() -> matchingRepository.findById(matchingId)));
    }

    private Mono<Matching> updateMatchStatus(final Matching matching) {
        matching.updateStatus();
        if (!matching.isWait()) {
            if (matching.isSuccess() && matching.isNullBattleId()) {
                final List<String> memberIds =
                        matching.getMembers().stream().map(MatchingMember::getId).toList();
                log.info("{}", memberIds);
                matching.setBattleId(
                        battleService
                                .create(memberIds, matching.getDistance())
                                .block()); // TODO blocking 로직 제거
            }
            return matchingRepository.save(matching);
        }
        return Mono.just(matching);
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
