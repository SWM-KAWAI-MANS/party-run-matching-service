package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEvent;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;
import online.partyrun.partyrunmatchingservice.global.sse.ServerSentEventHandler;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaitingService {
    ServerSentEventHandler<String, WaitingEvent> sseHandler;

    public Mono<MessageResponse> create(Mono<String> member, CreateWaitingRequest request) {
        return member.map(
                id -> {
                    sseHandler.create(id);
                    // TODO 대기열 요청 등록
                    return new MessageResponse(id + "님 대기열 등록");
                });
    }
}