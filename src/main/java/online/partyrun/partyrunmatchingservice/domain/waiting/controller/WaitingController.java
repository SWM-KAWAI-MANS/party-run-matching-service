package online.partyrun.partyrunmatchingservice.domain.waiting.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.partyrunmatchingservice.domain.waiting.service.WaitingService;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * waiting 관련 요청 및 응답을 관리합니다.
 *
 * @author parkhyeonjun
 * @see WaitingService
 * @since 2023.06.29
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("waiting")
public class WaitingController {
    WaitingService waitingService;

    /**
     * waiting 생성을 요청합니다.
     *
     * @param auth 로그인한 유저 정보
     * @param request 대기 생성시 요구사항
     * @return a deferred message, success시 201 상태 {@link Mono} {@link MessageResponse}
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MessageResponse> postWaitingRunner(
            Mono<Authentication> auth, @RequestBody CreateWaitingRequest request) {
        return waitingService.register(auth.map(Principal::getName), request);
    }

    /**
     * waiting event 구독을 진행합니다. complete 전까지 연결된 sink에 event가 추가될 때마다 사용자에게 전달합니다.
     *
     * @param auth 로그인한 유저 정보
     * @return event stream, success시 200 상태 {@link Flux} {@link WaitingEventResponse}
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @GetMapping(path = "event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Flux<WaitingEventResponse> getEventSteam(Mono<Authentication> auth) {
        return waitingService.subscribe(auth.map(Principal::getName));
    }
}
