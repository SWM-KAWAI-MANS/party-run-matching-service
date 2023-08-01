package online.partyrun.partyrunmatchingservice.domain.waiting.queue;

import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;

import java.util.List;

public interface WaitingQueue {
    void add(WaitingMember user);

    boolean isSatisfyCount(RunningDistance distance);

    List<String> poll(RunningDistance distance);
}
