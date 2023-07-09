package online.partyrun.application.domain.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingUser;

import org.junit.jupiter.api.*;

@DisplayName("InMemorySubscribeBuffer ")
class InMemorySubscribeBufferTest {
    SubscribeBuffer buffer = new InMemorySubscribeBuffer();
    WaitingUser 현준 = new WaitingUser("현준", RunningDistance.M1000);
    WaitingUser 성우 = new WaitingUser("성우", RunningDistance.M1000);
    WaitingUser 준혁 = new WaitingUser("준혁", RunningDistance.M1000);
    WaitingUser 세연 = new WaitingUser("세연", RunningDistance.M3000);
    WaitingUser 승열 = new WaitingUser("승열", RunningDistance.M3000);
    WaitingUser 현식 = new WaitingUser("현식", RunningDistance.M5000);

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class user를_추가한_후에 {
        @BeforeEach
        void addUser() {
            buffer.add(현준);
            buffer.add(성우);
            buffer.add(세연);
            buffer.add(준혁);
            buffer.add(승열);
            buffer.add(현식);
        }

        @Test
        @DisplayName("각 거리 별 개수가 만족하는지 측정한다")
        void runAddUser() {
            assertAll(
                    () -> assertThat(buffer.satisfyCount(RunningDistance.M1000, 3)).isTrue(),
                    () -> assertThat(buffer.satisfyCount(RunningDistance.M1000, 5)).isFalse(),
                    () -> assertThat(buffer.satisfyCount(RunningDistance.M3000, 2)).isTrue(),
                    () -> assertThat(buffer.satisfyCount(RunningDistance.M5000, 1)).isTrue());
        }

        @Test
        @DisplayName("각 거리 별로 원하는 개수만큼 추출한다")
        void runFlush() {
            assertAll(
                    () ->
                            assertThat(buffer.flush(RunningDistance.M1000, 3))
                                    .contains("현준", "준혁", "성우"),
                    () -> assertThat(buffer.flush(RunningDistance.M3000, 2)).contains("승열", "세연"),
                    () -> assertThat(buffer.flush(RunningDistance.M5000, 1)).contains("현식"));
        }

        @Test
        @DisplayName("주어진 요소보다 추출 개수가 많으면 에러를 반환한다")
        void validateElementCount() {}

        @Test
        @DisplayName("이미 중복된 유저가 지원하면 에러를 반환한다")
        void validateDuplicateUser() {}
    }
}
