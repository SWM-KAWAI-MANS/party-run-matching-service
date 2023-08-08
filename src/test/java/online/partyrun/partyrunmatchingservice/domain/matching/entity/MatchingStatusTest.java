package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("MatchingStatus에서")
class MatchingStatusTest {
    @Test
    @DisplayName("wait 상황이면 true를 반환한다")
    void returnTrue() {
        assertAll(
                () -> assertThat(MatchingStatus.WAIT.isWait()).isTrue(),
                () -> assertThat(MatchingStatus.CANCEL.isWait()).isFalse(),
                () -> assertThat(MatchingStatus.SUCCESS.isWait()).isFalse()
        );
    }
}