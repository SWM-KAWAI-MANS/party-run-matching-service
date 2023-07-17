package online.partyrun.partyrunmatchingservice.domain.match.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.match.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.match.dto.MatchRequest;
import online.partyrun.partyrunmatchingservice.domain.match.service.MatchService;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * match 관련 요청 및 응답을 관리합니다.
 *
 * @author parkhyeonjun
 * @see MatchService
 * @since 2023.06.29
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("match")
public class MatchController {
    MatchService matchService;

    /**
     * match status 상태를 변경합니다.
     *
     * @param auth 로그인한 유저 정보
     * @param request 상태 요구사항
     * @return a deferred message, success시 201 상태 {@link Mono} {@link MessageResponse}
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MessageResponse> postMatchStatus(
            Mono<Authentication> auth, @RequestBody MatchRequest request) {
        return matchService
                .setMemberStatus(auth.map(Principal::getName), request)
                .flatMap(i -> Mono.just(new MessageResponse("참여여부 등록")));
    }

    /**
     * match 관련 event를 구독합니다. complete 전까지 연결된 sink에 event가 추가될 때마다 사용자에게 전달합니다.
     *
     * @param auth 로그인한 유저 정보
     * @return event stream, success시 200 상태 {@link Flux} {@link WaitingEventResponse}
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @GetMapping(path = "event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MatchEvent> match(Mono<Authentication> auth) {
        return matchService.subscribe(auth.map(Principal::getName));
    }
}