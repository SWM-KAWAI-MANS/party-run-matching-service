package online.partyrun.application.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import online.partyrun.application.domain.waiting.domain.WaitingEvent;
import online.partyrun.application.domain.waiting.domain.WaitingUser;
import online.partyrun.application.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.application.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.application.domain.waiting.message.WaitingPublisher;
import online.partyrun.application.global.dto.MessageResponse;
import online.partyrun.application.global.handler.ServerSentEventHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class WaitingService {
    WaitingPublisher waitingPublisher;
    ServerSentEventHandler<String, WaitingEvent> waitingEventHandler;


    public Mono<MessageResponse> register(Mono<String> runner, CreateWaitingRequest request) {
        return runner.doOnNext(
                id -> {
                    log.info("{}", id);
                    waitingEventHandler.addSink(id);
                    waitingPublisher.publish(new WaitingUser(id, request.distance()));
                })
                .map(id -> new MessageResponse(id + "님 대기열 등록"));
    }

    /**
     * 대기열을 구독합니다. sink에 연결한 후, 들어오는 event를 사용자에게 바로 전달합니다.
     *
     * @param runner 사용자 id가 담겨있는 {@link Mono}
     * @return event stream
     * @author Hyeonjun Park
     * @since 2023-06-29
     */
    public Flux<WaitingEventResponse> subscribe(Mono<String> runner) {
        return runner.map(
                        id ->
                                waitingEventHandler
                                        .connect(id)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .doOnNext(event -> {
                                            log.info("{}", event.getMessage());
                                            if(!event.equals(WaitingEvent.CONNECT)) {
                                                waitingEventHandler.disconnect(id);
                                            }
                                        })
                                        .map(WaitingEventResponse::new))
                .flatMapMany(f -> f);
    }
}
