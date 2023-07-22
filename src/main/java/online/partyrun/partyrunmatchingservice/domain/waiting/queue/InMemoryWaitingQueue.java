package online.partyrun.partyrunmatchingservice.domain.waiting.queue;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.waiting.exception.DuplicateUserException;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.NotSatisfyCountException;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingUser;

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

    @Override
    public void add(final WaitingUser user) {
        if (hasElement(user.userId())) {
            throw new DuplicateUserException();
        }
        map.get(user.distance()).add(user.userId());
    }

    private boolean hasElement(final String element) {
        return Arrays.stream(RunningDistance.values())
                .map(map::get)
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
