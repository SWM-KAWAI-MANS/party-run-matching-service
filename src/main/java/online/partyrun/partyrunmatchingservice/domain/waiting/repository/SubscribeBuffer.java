package online.partyrun.partyrunmatchingservice.domain.waiting.repository;

import online.partyrun.partyrunmatchingservice.domain.waiting.domain.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.domain.WaitingUser;

import java.util.List;

public interface SubscribeBuffer {
    List<String> flush(RunningDistance distance, int count);

    boolean satisfyCount(RunningDistance distance, int count);

    void add(WaitingUser user);

    boolean hasElement(String element);
}
