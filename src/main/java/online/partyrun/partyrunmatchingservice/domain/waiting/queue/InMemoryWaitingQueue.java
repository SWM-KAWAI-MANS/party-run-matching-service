package online.partyrun.partyrunmatchingservice.domain.waiting.queue;

import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.DuplicateMemberException;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.NotSatisfyCountException;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryWaitingQueue implements WaitingQueue {
    private static final int SATISFY_COUNT = 2;
    Map<RunningDistance, LinkedList<String>> map = new ConcurrentHashMap<>();

    public InMemoryWaitingQueue() {
        Arrays.stream(RunningDistance.values())
                .forEach(distance -> map.put(distance, new LinkedList<>()));
    }

    @Synchronized
    @Override
    public void add(final WaitingMember member) {
        if (hasMemberId(member.memberId())) {
            throw new DuplicateMemberException(member.memberId());
        }
        map.get(member.distance()).add(member.memberId());
    }

    private boolean hasMemberId(final String memberId) {
        return map.values().stream().anyMatch(q -> q.contains(memberId));
    }

    @Override
    public boolean isSatisfyCount(final RunningDistance distance) {
        return map.get(distance).size() >= SATISFY_COUNT;
    }

    @Override
    public List<String> poll(final RunningDistance distance) {
        if (!isSatisfyCount(distance)) {
            throw new NotSatisfyCountException();
        }
        return IntStream.range(0, SATISFY_COUNT).mapToObj(i -> map.get(distance).poll()).toList();
    }

    @Override
    public boolean hasMember(String memberId) {
        return Arrays.stream(RunningDistance.values())
                .map(map::get)
                .anyMatch(q -> q.contains(memberId));
    }

    @Override
    public void clear() {
        Arrays.stream(RunningDistance.values()).forEach(distance -> map.get(distance).clear());
    }

    @Override
    public void delete(String memberId) {
        Arrays.stream(RunningDistance.values())
                .map(map::get)
                .forEach(queue -> queue.remove(memberId));
    }
}
