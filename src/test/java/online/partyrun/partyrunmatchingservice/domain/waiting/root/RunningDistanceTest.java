package online.partyrun.partyrunmatchingservice.domain.waiting.root;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import online.partyrun.partyrunmatchingservice.domain.waiting.exception.NotAllowDistanceException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("RunningDistance")
class RunningDistanceTest {
    public static Stream<Arguments> distanceElements() {
        return Stream.of(
                Arguments.of(1000, RunningDistance.M1000),
                Arguments.of(3000, RunningDistance.M3000),
                Arguments.of(5000, RunningDistance.M5000),
                Arguments.of(10000, RunningDistance.M10000));
    }

    @ParameterizedTest
    @MethodSource("distanceElements")
    @DisplayName("거리를 숫자로 입력하면 해당하는 Dinstance 요소를 반환한다")
    void runGetByMeter(int meter, RunningDistance distance) {
        assertThat(RunningDistance.getBy(meter)).isEqualTo(distance);
    }

    @Test
    @DisplayName("허용하지 않은 숫자를 입력하면 예외를 반환한다")
    void throwNotAllowDistanceException() {
        assertThatThrownBy(() -> RunningDistance.getBy(2500))
                .isInstanceOf(NotAllowDistanceException.class);
    }
}
