package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.matching.controller.MatchingRequest;
import online.partyrun.partyrunmatchingservice.domain.matching.service.MatchingService;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis.WaitingQueue;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
        return member.flatMapMany(this::connectSink)
                .map(WaitingEventResponse::new);
    }

    private Flux<WaitingStatus> connectSink(final String id) {
        return waitingSinkHandler
                .connect(id)
                .doOnNext(status -> checkComplete(id, status));
    }

    private void checkComplete(final String id, final WaitingStatus status) {
        if (status.isCompleted()) {
            waitingSinkHandler.complete(id);
        }
    }

    public void sendMatchEvent(List<String> members) {
        members.forEach(member -> waitingSinkHandler.sendEvent(member, WaitingStatus.MATCHED));
    }

    @Scheduled(fixedDelay = REMOVE_SINK_SCHEDULE_TIME, initialDelay = REMOVE_SINK_SCHEDULE_TIME) // 12시간 마다 실행
    public void removeUnConnectedSink() {
        Flux.fromIterable(waitingSinkHandler.getConnectors())
                .parallel()
                .flatMap(member -> waitingQueue.hasMember(member)
                        .filter(is -> is)
                        .flatMap(is -> checkDisconnectAndSendMatchingFalse(member))
                        .then())
                .subscribe();
    }

    private Mono<Void> checkDisconnectAndSendMatchingFalse(final String key) {
        waitingSinkHandler.disconnectIfExist(key);
        return matchingService
                .setMemberStatus(Mono.just(key), new MatchingRequest(false))
                .then();
    }

    public Mono<Void> shutdown() {
        waitingSinkHandler.shutdown();
        return waitingQueue.clear().then();
    }

    public Mono<MessageResponse> cancel(final Mono<String> member) {
        return member.flatMap(this::checkDisconnectAndDeleteWaiting)
                .then(Mono.just(new MessageResponse("cancelled")));
    }

    private Mono<Void> checkDisconnectAndDeleteWaiting(final String memberId) {
        waitingSinkHandler.sendEvent(memberId, WaitingStatus.CANCEL);
        return waitingQueue.delete(memberId);
    }

    public void sendMatchEventIfExist(String member) {
        if(waitingSinkHandler.isExist(member)) {
            waitingSinkHandler.sendEvent(member, WaitingStatus.MATCHED);
        }
    }
}
