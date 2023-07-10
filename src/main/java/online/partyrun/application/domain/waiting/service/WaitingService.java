package online.partyrun.application.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import online.partyrun.application.domain.match.service.MatchService;
import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingEvent;
import online.partyrun.application.domain.waiting.domain.WaitingUser;
import online.partyrun.application.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.application.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.application.domain.waiting.message.WaitingPublisher;
import online.partyrun.application.domain.waiting.repository.SubscribeBuffer;
import online.partyrun.application.global.dto.MessageResponse;
import online.partyrun.application.global.handler.ServerSentEventHandler;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class WaitingService implements MessageListener {
    WaitingPublisher waitingPublisher;
    ServerSentEventHandler<String, WaitingEvent> waitingEventHandler;
    SubscribeBuffer buffer;
    MatchService matchService;
    RedisSerializer<WaitingUser> serializer;

    static int SATISFY_COUNT = 2;

    public Mono<MessageResponse> register(Mono<String> runner, CreateWaitingRequest request) {
        return runner.handle((id, sink) -> {
            if(buffer.hasElement(id)) {
                sink.error(new IllegalArgumentException());
                return;
            }
            waitingEventHandler.addSink(id);
            waitingPublisher.publish(new WaitingUser(id, request.distance()));
            sink.next(new MessageResponse(id + "님 대기열 등록"));
        });
    }

    /**
     * 대기열을 구독합니다. sink에 연결한 후, 들어오는 event를 사용자에게 바로 전달합니다.
     *
     * @param member 사용자 id가 담겨있는 {@link Mono}
     * @return event stream
     * @author Hyeonjun Park
     * @since 2023-06-29
     */
    public Flux<WaitingEventResponse> subscribe(Mono<String> member) {
        return member.map(
                        id ->
                                waitingEventHandler
                                        .connect(id)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .doOnNext(
                                                event -> {
                                                    if (!event.equals(WaitingEvent.CONNECT)) {
                                                        waitingEventHandler.complete(id);
                                                    }
                                                })
                                        .map(WaitingEventResponse::new))
                .flatMapMany(f -> f);
    }

    @Override
    public void onMessage(final Message message, final byte[] pattern) {
        buffer.add(serializer.deserialize(message.getBody()));
        processMessages();
    }

    private void processMessages() {
        log.info("processMessages started");
        Arrays.stream(RunningDistance.values())
                .forEach(
                        distance -> {
                            if (buffer.satisfyCount(distance, SATISFY_COUNT)) {
                                List<String> memberIds = buffer.flush(distance, SATISFY_COUNT);
                                // 매칭 생성 보내기
                                matchService.create(memberIds, distance).subscribe();
                                // Event 추가하기
                                memberIds.forEach(
                                        m ->
                                                waitingEventHandler.sendEvent(
                                                        m, WaitingEvent.MATCHED));
                            }
                        });
    }
}
