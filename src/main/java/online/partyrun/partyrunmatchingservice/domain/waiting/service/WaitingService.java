package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.battle.service.BattleService;
import online.partyrun.partyrunmatchingservice.domain.battle.service.external.exception.RunnerAlreadyRunningException;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.message.WaitingMessagePublisher;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaitingService {
    WaitingEventService eventService;
    BattleService battleService;
    WaitingMessagePublisher messagePublisher;

    public Mono<MessageResponse> create(Mono<String> member, CreateWaitingRequest request) {
        return member.flatMap(id ->
                battleService.isRunning(id).flatMap(isRunning -> {
                    if (isRunning) {
                        return Mono.error(new RunnerAlreadyRunningException());
                    }
                    eventService.register(id);
                    messagePublisher.publish(new WaitingMember(id, request.distance()));
                    return Mono.just(new MessageResponse(id + "님 대기열 등록"));
                })
        );
    }
}
