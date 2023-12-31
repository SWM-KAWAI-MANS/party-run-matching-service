package online.partyrun.partyrunmatchingservice.domain.matching.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchingMembersResponse;
import online.partyrun.partyrunmatchingservice.domain.matching.service.MatchingService;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("matching")
public class MatchingController {
    MatchingService matchingService;

    @PostMapping("members/join")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MessageResponse> postMatchStatus(
            Mono<Authentication> auth, @RequestBody MatchingRequest request) {
        return matchingService
                .setMemberStatus(auth.map(Principal::getName), request)
                .then(Mono.defer(() -> Mono.just(new MessageResponse("참여여부 등록"))));
    }

    @GetMapping(path = "event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MatchEvent> getEventSteam(Mono<Authentication> auth) {
        return matchingService.getEventSteam(auth.map(Principal::getName));
    }

    @GetMapping("recent/members")
    public Mono<MatchingMembersResponse> getBy(Mono<Authentication> auth) {
        return matchingService.getResent(auth.map(Principal::getName));
    }
}
