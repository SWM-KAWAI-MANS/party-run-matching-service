package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.service.MatchingService;
import online.partyrun.partyrunmatchingservice.domain.waiting.message.WaitingMessagePublisher;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis.WaitingQueue;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingCheckService {
    WaitingMessagePublisher publisher;
    WaitingQueue waitingQueue;
    MatchingService matchingService;

    public Mono<Matching> check(RunningDistance distance) {
        return waitingQueue.findNextGroup(distance)
                .doOnNext(members -> members.forEach(publisher::publish))
                .flatMap(members -> {
                            if(members.isEmpty()) {
                                return Mono.empty();
                            }
                            return matchingService.create(members, distance.getMeter());
                        }
                );
    }
}
