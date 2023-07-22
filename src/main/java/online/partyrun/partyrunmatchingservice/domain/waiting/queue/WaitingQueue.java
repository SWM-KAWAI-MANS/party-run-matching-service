package online.partyrun.partyrunmatchingservice.domain.waiting.queue;

import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingUser;

import java.util.List;

public interface WaitingQueue {
    void add(WaitingUser user);

    boolean satisfyCount(RunningDistance distance);

    List<String> poll(RunningDistance distance);
}
