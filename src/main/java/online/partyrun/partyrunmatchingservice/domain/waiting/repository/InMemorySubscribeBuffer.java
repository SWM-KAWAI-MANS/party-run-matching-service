package online.partyrun.partyrunmatchingservice.domain.waiting.repository;

import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.waiting.domain.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.domain.WaitingUser;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.DuplicateUserException;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.OutOfSizeBufferException;

import online.partyrun.partyrunmatchingservice.domain.waiting.service.SubscribeBuffer;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemorySubscribeBuffer implements SubscribeBuffer {

    Map<RunningDistance, Queue<String>> map = new ConcurrentHashMap<>();

    public InMemorySubscribeBuffer() {
        Arrays.stream(RunningDistance.values())
                .forEach(distance -> map.put(distance, new LinkedList<>()));
    }

    @Synchronized
    @Override
    public List<String> flush(RunningDistance distance, int count) {
        if (map.get(distance).size() < count) {
            throw new OutOfSizeBufferException();
        }
        return IntStream.range(0, count).mapToObj(i -> map.get(distance).poll()).toList();
    }

    @Override
    public boolean satisfyCount(final RunningDistance distance, int count) {
        return map.get(distance).size() >= count;
    }

    @Synchronized
    @Override
    public void add(final WaitingUser user) {
        if (hasElement(user.userId())) {
            throw new DuplicateUserException();
        }
        map.get(user.distance()).add(user.userId());
    }

    @Override
    public boolean hasElement(final String element) {
        return Arrays.stream(RunningDistance.values())
                .map(map::get)
                .anyMatch(q -> q.contains(element));
    }
}
