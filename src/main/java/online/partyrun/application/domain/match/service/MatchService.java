package online.partyrun.application.domain.match.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import online.partyrun.application.domain.match.domain.Match;
import online.partyrun.application.domain.match.domain.MatchMember;
import online.partyrun.application.domain.match.domain.MatchStatus;
import online.partyrun.application.domain.match.dto.MatchEvent;
import online.partyrun.application.domain.match.dto.MatchRequest;
import online.partyrun.application.domain.match.repository.MatchRepository;
import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.global.handler.ServerSentEventHandler;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    MatchRepository matchRepository;
    ServerSentEventHandler<String, MatchEvent> matchEventHandler;

    public Mono<Match> setMemberStatus(final Mono<String> member, final MatchRequest request) {
        return member.flatMap(
                mid ->
                        matchRepository
                                .findByMembersIdAndStatus(mid, MatchStatus.WAIT)
                                .flatMap(
                                        match -> {
                                            log.info("setMemberStatus {}", match.getStatus());
                                            match.updateMemberStatus(mid, request.isJoin());
                                            return matchRepository.save(match);
                                        })
                                .doOnSuccess(this::sendEvent));
    }

    private void sendEvent(final Match match) {
        log.info("sendEvent {}", match.getStatus());
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
                                                    log.info("{}", event.status());
                                                    log.info("{}", event.members());
                                                    if (!event.status().equals(MatchStatus.WAIT)) {
                                                        matchEventHandler.complete(id);
                                                    }
                                                }))
                .flatMapMany(f -> f);
    }

    public Mono<Match> create(final List<String> memberIds, final RunningDistance distance) {
        final List<MatchMember> members = memberIds.stream().map(MatchMember::new).toList();
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
}
