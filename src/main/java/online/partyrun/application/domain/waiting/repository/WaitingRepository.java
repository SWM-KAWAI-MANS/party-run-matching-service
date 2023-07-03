package online.partyrun.application.domain.waiting.repository;

import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingRunner;

import java.util.List;

/**
 * Waiting 저장소를 구현하는 interface 입니다.
 *
 * @author parkhyeonjun
 * @see online.partyrun.application.domain.waiting.repository.queue.InMemoryWaitingQueue
 * @since 2023.06.29
 */
public interface WaitingRepository {
    void save(final WaitingRunner waiting);

    List<String> findTop(final RunningDistance distance, final int count);

    int size(final RunningDistance distance);
}
