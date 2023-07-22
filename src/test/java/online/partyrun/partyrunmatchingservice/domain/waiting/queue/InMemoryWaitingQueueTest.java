package online.partyrun.partyrunmatchingservice.domain.waiting.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import online.partyrun.partyrunmatchingservice.domain.waiting.exception.DuplicateUserException;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.NotSatisfyCountException;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingUser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InMemoryWaitingQueue")
class InMemoryWaitingQueueTest {
    WaitingQueue waitingQueue = new InMemoryWaitingQueue();
    WaitingUser 현준 = new WaitingUser("현준", 1000);
    WaitingUser 성우 = new WaitingUser("셩우", 1000);

    @Test
    @DisplayName("큐에 추가를 수행한 후 개수가 만족한 지 확인한다")
    void runAdd() {
        waitingQueue.add(현준);
        assertThat(waitingQueue.satisfyCount(현준.distance())).isFalse();
        waitingQueue.add(성우);
        assertThat(waitingQueue.satisfyCount(현준.distance())).isTrue();
    }

    @Test
    @DisplayName("중복된 사용자를 추가하면 예외를 반환한다")
    void throwDuplicateUserException() {
        waitingQueue.add(현준);
        assertThatThrownBy(() -> waitingQueue.add(현준)).isInstanceOf(DuplicateUserException.class);
    }

    @Test
    @DisplayName("만족하는 개수만큼 추출을 받는다")
    void runPoll() {
        waitingQueue.add(현준);
        waitingQueue.add(성우);

        assertThat(waitingQueue.poll(현준.distance())).contains(현준.userId(), 성우.userId());
    }

    @Test
    @DisplayName("일정 개수에 만족하지 못하면 예외를 반환한다")
    void throwNotSatisfyCount() {
        waitingQueue.add(현준);
        assertThatThrownBy(() -> waitingQueue.poll(현준.distance()))
                .isInstanceOf(NotSatisfyCountException.class);
    }
}
