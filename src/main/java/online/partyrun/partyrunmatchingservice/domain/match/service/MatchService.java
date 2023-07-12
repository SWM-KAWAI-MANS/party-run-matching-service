package online.partyrun.partyrunmatchingservice.domain.match.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.match.domain.Match;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MatchMember;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MatchStatus;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MemberStatus;
import online.partyrun.partyrunmatchingservice.domain.match.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.match.dto.MatchRequest;
import online.partyrun.partyrunmatchingservice.domain.match.repository.MatchRepository;
import online.partyrun.partyrunmatchingservice.domain.waiting.domain.RunningDistance;
import online.partyrun.partyrunmatchingservice.global.handler.ServerSentEventHandler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MatchService {
    MatchRepository matchRepository;
    ServerSentEventHandler<String, MatchEvent> matchEventHandler;
    Clock clock;

    public Mono<Match> setMemberStatus(final Mono<String> member, final MatchRequest request) {
        return member.flatMap(
                mid ->
                        matchRepository
                                .findByMembersIdAndMembersStatus(mid, MemberStatus.NO_RESPONSE)
                                .flatMap(
                                        match -> {
                                            match.updateMemberStatus(mid, request.isJoin());

                                            return matchRepository.save(match);
                                        })
                                .doOnSuccess(this::sendEvent));
    }

    private void sendEvent(final Match match) {
        match.getMembers()
                .forEach(
                        member ->
                                matchEventHandler.sendEvent(member.getId(), new MatchEvent(match)));
    }

    public Flux<MatchEvent> subscribe(final Mono<String> member) {
        return member.map(
                        id ->
                                matchEventHandler
                                        .connect(id)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .doOnNext(
                                                event -> {
                                                    if (!event.status().equals(MatchStatus.WAIT)) {
                                                        matchEventHandler.complete(id);
                                                    }
                                                }))
                .flatMapMany(f -> f);
    }

    public Mono<Match> create(final List<String> memberIds, final RunningDistance distance) {
        final List<MatchMember> members = memberIds.stream().map(MatchMember::new).toList();
        memberIds.forEach(
                member ->
                        matchRepository
                                .findByMembersIdAndMembersStatus(member, MemberStatus.NO_RESPONSE)
                                .subscribe(
                                        match -> {
                                            match.updateMemberStatus(member, false);
                                            matchEventHandler.complete(member);
                                        }));

        return matchRepository
                .save(new Match(members, distance.getMeter()))
                .doOnSuccess(
                        match ->
                                members.forEach(
                                        member -> {
                                            matchEventHandler.addSink(member.getId());
                                            matchEventHandler.sendEvent(
                                                    member.getId(), new MatchEvent(match));
                                        }));
    }

    @Scheduled(fixedDelay = 3_600_000) // 3시간 마다 실행
    public void removeUnConnectedSink() {
        LocalDateTime now = LocalDateTime.now(clock);
        matchRepository
                .findAllByStatus(MatchStatus.WAIT)
                .doOnNext(
                        match -> {
                            if (match.getStartAt().isBefore(now.minusHours(2))) {
                                match.getMembers()
                                        .forEach(
                                                member -> {
                                                    match.updateMemberStatus(member.getId(), false);
                                                    matchEventHandler.complete(member.getId());
                                                    matchRepository.save(match).subscribe();
                                                });
                            }
                        })
                .blockLast();
    }
}
