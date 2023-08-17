package online.partyrun.partyrunmatchingservice.domain.waiting.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.partyrunmatchingservice.domain.waiting.service.WaitingEventService;
import online.partyrun.partyrunmatchingservice.domain.waiting.service.WaitingService;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("waiting")
public class WaitingController {
    WaitingService waitingService;
    WaitingEventService waitingEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MessageResponse> postWaitingRunner(
            Mono<Authentication> auth, @Valid @RequestBody CreateWaitingRequest request) {
        return waitingService.create(auth.map(Principal::getName), request);
    }

    @GetMapping(path = "event", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public Flux<WaitingEventResponse> getEventSteam(Mono<Authentication> auth) {
        return this.waitingEventService.getEventStream(auth.map(Principal::getName));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWaiting() {
        waitingEventService.shutdown();
    }

    @PostMapping("event/cancel")
    @ResponseStatus(HttpStatus.OK)
    public Mono<MessageResponse> cancelEvent(Mono<Authentication> auth) {
        return waitingEventService.cancel(auth.map(Principal::getName));
    }
}
