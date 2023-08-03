package online.partyrun.partyrunmatchingservice.domain.waiting.queue;

import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;

import java.util.List;

public interface WaitingQueue {
    void add(WaitingMember member);

    boolean isSatisfyCount(RunningDistance distance);

    List<String> poll(RunningDistance distance);

    boolean hasMember(String memberId);

    void clear();
}
