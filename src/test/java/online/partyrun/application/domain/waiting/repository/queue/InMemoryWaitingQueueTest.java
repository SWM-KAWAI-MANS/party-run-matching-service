package online.partyrun.application.domain.waiting.repository.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingRunner;

import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

@DisplayName("InMemoryWaitingQueue")
class InMemoryWaitingQueueTest {
    InMemoryWaitingQueue queue = new InMemoryWaitingQueue();

    @AfterEach
    public void clear() {
        Arrays.stream(RunningDistance.values()).forEach(rd -> queue.findTop(rd, queue.size(rd)));
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class WaitingRunner가_주어지면 {
        List<WaitingRunner> m1000Runners =
                List.of(
                        new WaitingRunner("userId1", RunningDistance.M1000),
                        new WaitingRunner("userId2", RunningDistance.M1000),
                        new WaitingRunner("userId3", RunningDistance.M1000),
                        new WaitingRunner("userId4", RunningDistance.M1000));

        List<WaitingRunner> m3000Runners =
                List.of(
                        new WaitingRunner("userId5", RunningDistance.M3000),
                        new WaitingRunner("userId6", RunningDistance.M3000),
                        new WaitingRunner("userId7", RunningDistance.M3000));

        List<WaitingRunner> m5000Runners =
                List.of(
                        new WaitingRunner("userId8", RunningDistance.M5000),
                        new WaitingRunner("userId9", RunningDistance.M5000));

        @Test
        @DisplayName("저장을 수행한 후 개수를 확인한다.")
        void saveAndCheckSize() {
            m1000Runners.forEach(queue::save);
            m3000Runners.forEach(queue::save);
            m5000Runners.forEach(queue::save);

            assertAll(
                    () ->
                            assertThat(queue.size(RunningDistance.M1000))
                                    .isEqualTo(m1000Runners.size()),
                    () ->
                            assertThat(queue.size(RunningDistance.M3000))
                                    .isEqualTo(m3000Runners.size()),
                    () ->
                            assertThat(queue.size(RunningDistance.M5000))
                                    .isEqualTo(m5000Runners.size()));
        }

        @Test
        @DisplayName("저장을 수행한 후 원하는 개수만큼 추출한다.")
        void saveAndCheckFindTop() {
            m1000Runners.forEach(queue::save);
            final List<String> result = queue.findTop(RunningDistance.M1000, 3);

            assertThat(result).hasSize(3);
        }
    }
}
