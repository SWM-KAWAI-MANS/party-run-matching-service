package online.partyrun.partyrunmatchingservice.global.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import online.partyrun.partyrunmatchingservice.global.sse.exception.KeyNotExistException;
import online.partyrun.partyrunmatchingservice.global.sse.exception.NullKeyException;

import org.junit.jupiter.api.*;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("MultiSinkHandler")
class MultiSinkHandlerTest {
    MultiSinkHandler<String, String> multiSinkHandler = new MultiSinkHandler<>();
    final String 현준 = "현준";
    final String 성우 = "성우";
    final String 준혁 = "준혁";

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 한_명에_대한_싱크를_생셩했을_때 {

        @BeforeEach
        public void setUp() {
            multiSinkHandler.create(현준);
        }

        @Test
        @DisplayName("sink를 추가한 다음 connect를 수행한 후 완료를 진행한다.")
        void runConnect() {
            final Flux<String> connected = multiSinkHandler.connect(현준);
            multiSinkHandler.complete(현준);

            StepVerifier.create(connected).verifyComplete();
        }

        @Test
        @DisplayName("event 추가를 수행한다")
        void runAddEvent() {
            final String event1 = "이벤트 시작";
            final String event2 = "이벤트 마지막";
            final Flux<String> connected = multiSinkHandler.connect(현준);
            multiSinkHandler.sendEvent(현준, event1);
            multiSinkHandler.sendEvent(현준, event2);
            multiSinkHandler.complete(현준);

            StepVerifier.create(connected).expectNext(event1, event2).verifyComplete();
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 여러_명이_생성되었을_때 {
        @BeforeEach
        void setup() {
            multiSinkHandler.create(현준);
            multiSinkHandler.create(성우);
            multiSinkHandler.create(준혁);
        }

        @Test
        @DisplayName("셧다운 진행 시 모든 sink 완료시키고 sink map을 clear한다")
        void runShutDown() {

            final Flux<String> connect = multiSinkHandler.connect(현준);

            multiSinkHandler.shutdown();

            assertThat(multiSinkHandler.getConnectors()).isEmpty();
            StepVerifier.create(connect).verifyComplete();
        }

        @Test
        @DisplayName("key 전체 조회를 수행한다.")
        void runGetConnectors() {

            assertThat(multiSinkHandler.getConnectors()).containsExactlyInAnyOrder(현준, 성우, 준혁);
        }
    }

    @Test
    @DisplayName("조회시에 map에 key가 존재하지 않으면 예외를 처리한다")
    void throwIfKeyNotExist() {
        assertThatThrownBy(() -> multiSinkHandler.complete("nullkey"))
                .isInstanceOf(KeyNotExistException.class);
    }

    @Test
    @DisplayName("key가 null일 경우 예외를 반환한다")
    void throwExceptionIfKeyNull() {
        assertAll(
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.connect(null).blockLast())
                                .isInstanceOf(NullKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.sendEvent(null, "test"))
                                .isInstanceOf(NullKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.complete(null))
                                .isInstanceOf(NullKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.create(null))
                                .isInstanceOf(NullKeyException.class));
    }
}
