package online.partyrun.partyrunmatchingservice.domain.party.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyEvent;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyIdResponse;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyRequest;
import online.partyrun.partyrunmatchingservice.domain.party.service.PartyService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("parties")
public class PartyController {
    PartyService partyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PartyIdResponse> postParties(Mono<Authentication> auth, @RequestBody PartyRequest request) {
        return partyService.create(auth.map(Principal::getName), request);
    }

    @GetMapping(path = "{entryCode}/join", produces = "text/event-stream")
    public Flux<PartyEvent> getPartyEventStream(Mono<Authentication> auth, @PathVariable String entryCode) {
        return partyService.getEventStream(auth.map(Principal::getName), entryCode);
    }

    @PostMapping("{entryCode}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> postPartyStart(Mono<Authentication> auth, @PathVariable String entryCode) {
        return partyService.start(auth.map(Principal::getName), entryCode);
    }

    @PostMapping("{entryCode}/quit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> postPartyQuit(Mono<Authentication> auth, @PathVariable String entryCode) {
        return partyService.quit(auth.map(Principal::getName), entryCode);
    }
}
