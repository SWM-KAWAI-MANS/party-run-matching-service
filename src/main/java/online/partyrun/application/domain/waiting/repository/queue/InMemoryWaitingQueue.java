package online.partyrun.application.domain.waiting.repository.queue;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingRunner;
import online.partyrun.application.domain.waiting.repository.WaitingRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

/**
 * {@link WaitingRepository}를 구현하는 Queue 입니다.
 * InMemory 환경에서 동작할 수 있는 큐로 구성했습니다.
 *
 * <p>
 * {@link EnumMap}을 통하여 각 {@link RunningDistance} 별 Queue를 별도로 구축했습니다.
 * </p>
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryWaitingQueue implements WaitingRepository {
    Map<RunningDistance, Queue<String>> waitingQueue = new EnumMap<>(RunningDistance.class);

    public InMemoryWaitingQueue() {
        Arrays.stream(RunningDistance.values())
                .forEach(rd -> waitingQueue.put(rd, new LinkedList<>()));
    }

    /**
     * queue에 runner를 저장합니다.
     * 각 {@link RunningDistance} 별로 저장을 수행합니다.
     *
     * @param waitingRunner 저장할 runner 정보
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public void save(final WaitingRunner waitingRunner) {
        waitingQueue.get(waitingRunner.distance()).add(waitingRunner.userId());
    }

    /**
     * {@link RunningDistance} 에 해당되는 Queue 중 count 개수 만큼 탐색을 진행합니다.
     *
     * @param distance 대상 대기열 distance
     * @param count    추출할 개수
     * @return 추출된 runnerId list
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public List<String> findTop(final RunningDistance distance, final int count) {
        Queue<String> queue = waitingQueue.get(distance);
        return IntStream.range(0, count)
                .mapToObj(i -> queue.poll())
                .toList();
    }

    /**
     * {@link RunningDistance} 에 해당되는 Queue의 요소 개수를 반환합니다.
     *
     * @param distance 대상 대기열 distance
     * @return 대상 Queue의 크기(Queue 요소의 개수)
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public int size(final RunningDistance distance) {
        return waitingQueue.get(distance).size();
    }
}
