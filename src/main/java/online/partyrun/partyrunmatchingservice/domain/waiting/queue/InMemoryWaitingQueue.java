package online.partyrun.partyrunmatchingservice.domain.waiting.queue;

import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.waiting.exception.DuplicateMemberException;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.NotSatisfyCountException;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryWaitingQueue implements WaitingQueue {
    private static final int SATISFY_COUNT = 2;
    Map<RunningDistance, Queue<String>> map = new ConcurrentHashMap<>();

    public InMemoryWaitingQueue() {
        Arrays.stream(RunningDistance.values())
                .forEach(distance -> map.put(distance, new LinkedList<>()));
    }

    @Synchronized
    @Override
    public void add(final WaitingMember member) {
        if (hasElement(member.memberId())) {
            throw new DuplicateMemberException(member.memberId());
        }
        map.get(member.distance()).add(member.memberId());
    }

    private boolean hasElement(final String element) {
        return map.values().stream()
                .anyMatch(q -> q.contains(element));
    }

    @Override
    public boolean satisfyCount(final RunningDistance distance) {
        return map.get(distance).size() >= SATISFY_COUNT;
    }

    @Override
    public List<String> poll(final RunningDistance distance) {
        if (!satisfyCount(distance)) {
            throw new NotSatisfyCountException();
        }
        return IntStream.range(0, SATISFY_COUNT).mapToObj(i -> map.get(distance).poll()).toList();
    }
}
