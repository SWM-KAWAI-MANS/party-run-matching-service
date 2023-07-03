package online.partyrun.application.domain.match.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Runner")
class RunnerTest {

    @Test
    @DisplayName("status 변경을 정상적으로 수행하는가")
    void successChangeStatus() {
        Runner runner = new Runner("test", "test", RunnerStatus.NO_RESPONSE);

        runner.changeStatus(RunnerStatus.REDDY);

        assertThat(runner.getStatus()).isEqualTo(RunnerStatus.REDDY);
    }
}