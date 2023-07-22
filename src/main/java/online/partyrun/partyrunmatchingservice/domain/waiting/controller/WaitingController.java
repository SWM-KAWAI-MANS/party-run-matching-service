package online.partyrun.partyrunmatchingservice.domain.waiting.controller;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MessageResponse> postWaitingRunner(
            Mono<Authentication> auth, @Valid @RequestBody CreateWaitingRequest request) {
        return waitingService.create(auth.map(Principal::getName), request);
    }

    @GetMapping(path = "event", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public Flux<WaitingStatus> getEventSteam(Mono<Authentication> auth) {
        return this.waitingService.getEventStream(auth.map(Principal::getName));
    }
}
