package online.partyrun.application.domain.waiting.repository;

import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingUser;

import java.util.List;

public interface SubscribeBuffer {
    List<String> flush(RunningDistance distance, int count);

    boolean satisfyCount(RunningDistance distance, int count);

    void add(WaitingUser user);
}
