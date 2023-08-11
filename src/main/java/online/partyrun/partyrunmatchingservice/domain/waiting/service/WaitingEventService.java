package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import online.partyrun.partyrunmatchingservice.domain.matching.controller.MatchingRequest;
import online.partyrun.partyrunmatchingservice.domain.matching.service.MatchingService;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.WaitingQueue;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingEventService {
    private static final int REMOVE_SINK_SCHEDULE_TIME = 14_400_000; // 12시간 마다 실행
    WaitingSinkHandler waitingSinkHandler;
    WaitingQueue waitingQueue;
    MatchingService matchingService;

    public void register(final String id) {
        waitingSinkHandler.create(id);
        waitingSinkHandler.sendEvent(id, WaitingStatus.CONNECTED);
    }

    public Flux<WaitingEventResponse> getEventStream(Mono<String> member) {
        return member.flatMapMany(
                        id ->
                                waitingSinkHandler
                                        .connect(id)
                                        .doOnNext(
                                                status -> {
                                                    if (status.isCompleted()) {
                                                        waitingSinkHandler.complete(id);
                                                    }
                                                }))
                .map(WaitingEventResponse::new);
    }

    public void sendMatchEvent(List<String> members) {
        members.forEach(member -> waitingSinkHandler.sendEvent(member, WaitingStatus.MATCHED));
    }

    @Scheduled(fixedDelay = REMOVE_SINK_SCHEDULE_TIME) // 12시간 마다 실행
    public void removeUnConnectedSink() {
        waitingSinkHandler.getConnectors().stream()
                .filter(connect -> !waitingQueue.hasMember(connect))
                .forEach(
                        key -> {
                            waitingSinkHandler.disconnectIfExist(key);
                            matchingService
                                    .setMemberStatus(Mono.just(key), new MatchingRequest(false))
                                    .subscribe();
                        });
    }

    public void shutdown() {
        waitingSinkHandler.shutdown();
        waitingQueue.clear();
    }

    public Mono<MessageResponse> cancel(final Mono<String> member) {
        return member.doOnNext(
                        memberId -> {
                            waitingSinkHandler.disconnectIfExist(memberId);
                            waitingQueue.delete(memberId);
                        })
                .then(Mono.just(new MessageResponse("cancelled")));
    }
}
