package online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingQueue {
    private static final int SATISFY_COUNT = 2;

    ReactiveListOperations<RunningDistance, String> waitingListOperations;

    public Mono<Void> add(final WaitingMember member) {
        return waitingListOperations.leftPush(member.distance(), member.memberId()).then();
    }

    public Mono<Boolean> isSatisfyCount(final RunningDistance distance) {
        return waitingListOperations.size(distance).map(count -> count >= SATISFY_COUNT);
    }

    public Mono<List<String>> poll(final RunningDistance distance) {
        return Flux.range(0, SATISFY_COUNT)
                .flatMap(i -> waitingListOperations.rightPop(distance))
                .collectList();
    }

    public Mono<Boolean> hasMember(final String memberId) {
        return Flux.fromArray(RunningDistance.values())
                .flatMap(distance -> waitingListOperations
                        .range(distance, 0, -1)
                        .any(member -> member.equals(memberId)))
                .hasElement(true);

    }

    public Mono<Void> clear() {
        return Flux.fromArray(RunningDistance.values())
                .flatMap(waitingListOperations::delete)
                .then();
    }

    public Mono<Void> delete(final String memberId) {
        return Flux.fromArray(RunningDistance.values())
                .flatMap(distance -> waitingListOperations.remove(distance, 1, memberId))
                .then();

    }
}