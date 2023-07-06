package online.partyrun.application.domain.waiting.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingUser;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.IntStream;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemorySubscribeBuffer implements SubscribeBuffer {

    Map<RunningDistance, Queue<String>> map = new EnumMap<>(RunningDistance.class);

    public InMemorySubscribeBuffer() {
        Arrays.stream(RunningDistance.values())
                .forEach(distance -> map.put(distance, new LinkedList<>()));
    }

    @Override
    public List<String> flush(RunningDistance distance, int count) {
        return IntStream.range(0, count).mapToObj(i -> map.get(distance).poll()).toList();
    }

    @Override
    public boolean satisfyCount(final RunningDistance distance, int count) {
        return map.get(distance).size() >= count;
    }

    @Override
    public void add(final WaitingUser user) {
        map.get(user.distance()).add(user.userId());
    }
}
