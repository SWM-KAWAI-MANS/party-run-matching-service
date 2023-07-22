package online.partyrun.partyrunmatchingservice.domain.waiting.root;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import online.partyrun.partyrunmatchingservice.domain.waiting.exception.NotAllowMeterException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RunningDistance")
class RunningDistanceTest {
    @Test
    @DisplayName("거리를 숫자로 입력하면 해당하는 Dinstance 요소를 반환한다")
    void runGetByMeter() {
        assertAll(
                () -> assertThat(RunningDistance.getByMeter(1000)).isEqualTo(RunningDistance.M1000),
                () -> assertThat(RunningDistance.getByMeter(3000)).isEqualTo(RunningDistance.M3000),
                () -> assertThat(RunningDistance.getByMeter(5000)).isEqualTo(RunningDistance.M5000),
                () ->
                        assertThat(RunningDistance.getByMeter(10000))
                                .isEqualTo(RunningDistance.M10000));
    }

    @Test
    @DisplayName("허용하지 않은 숫자를 입력하면 예외를 반환한다")
    void throwNotAllowMeterException() {
        assertThatThrownBy(() -> RunningDistance.getByMeter(2500))
                .isInstanceOf(NotAllowMeterException.class);
    }
}
