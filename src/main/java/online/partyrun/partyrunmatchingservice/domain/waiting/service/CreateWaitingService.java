package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis.WaitingQueue;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CreateWaitingService {
    WaitingEventService eventService;
    WaitingCheckService waitingCheckService;
    WaitingQueue waitingQueue;

    public Mono<MessageResponse> create(Mono<String> member, CreateWaitingRequest request) {
        return member.doOnNext(eventService::register)
                .flatMap(id ->  waitingQueue.add(new WaitingMember(id, request.distance())))
                .then(waitingCheckService.check(RunningDistance.getBy(request.distance())))
                .then(Mono.just(new MessageResponse("대기열 등록")));
    }
}
