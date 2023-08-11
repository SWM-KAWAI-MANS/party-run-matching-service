package online.partyrun.partyrunmatchingservice.domain.matching.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.matching.controller.MatchingRequest;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.global.sse.SinkHandlerTemplate;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MatchingSinkHandler extends SinkHandlerTemplate<String, MatchEvent> {
    MatchingService matchingService;

    @Override
    protected Runnable onCancel(String key) {
        return () ->
                matchingService
                        .setMemberStatus(Mono.just(key), new MatchingRequest(false))
                        .subscribe();
    }
}
