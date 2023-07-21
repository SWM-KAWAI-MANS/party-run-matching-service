package online.partyrun.partyrunmatchingservice.global.sse;

import online.partyrun.partyrunmatchingservice.global.sse.exception.InvalidSinksKeyException;
import online.partyrun.partyrunmatchingservice.global.sse.exception.SseConnectionException;
import org.junit.jupiter.api.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("MultiSinkHandler")
class MultiSinkHandlerTest {
    MultiSinkHandler<String, String> multiSinkHandler = new MultiSinkHandler<>();
    final String 현준 = "현준";
    final String 성우 = "성우";
    final String 준혁 = "준혁";

    @Test
    @DisplayName("sink를 추가한 다음 connect를 수행한 후 완료를 진행한다.")
    void runConnect() {
        multiSinkHandler.create(현준);
        final Flux<String> connected = multiSinkHandler.connect(현준);
        multiSinkHandler.complete(현준);

        StepVerifier.create(connected).verifyComplete();
    }

    @Test
    @DisplayName("event 추가를 수행한다")
    void runAddEvent() {
        final String event1 = "이벤트 시작";
        final String event2 = "이벤트 마지막";
        multiSinkHandler.create(현준);
        final Flux<String> connected = multiSinkHandler.connect(현준);
        multiSinkHandler.sendEvent(현준, event1);
        multiSinkHandler.sendEvent(현준, event2);
        multiSinkHandler.complete(현준);

        StepVerifier.create(connected).expectNext(event1, event2).verifyComplete();
    }

    @Test
    @DisplayName("sink 가져오기를 수행한다")
    void runGetSink() {
        multiSinkHandler.create(현준);
        final Sinks.Many<String> sink = multiSinkHandler.getSink(현준);
        sink.tryEmitComplete();

        StepVerifier.create(sink.asFlux()).expectNextCount(0).verifyComplete();
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
            assertAll(
                    () -> assertThat(multiSinkHandler.isExists(현준)).isFalse(),
                    () -> assertThat(multiSinkHandler.isExists(성우)).isFalse(),
                    () -> assertThat(multiSinkHandler.isExists(준혁)).isFalse()
            );
            StepVerifier.create(connect)
                    .verifyComplete();
        }

        @Test
        @DisplayName("key 전체 조회를 수행한다.")
        void runGetConnectors() {

            assertThat(multiSinkHandler.getConnectors()).containsExactlyInAnyOrder(현준, 성우, 준혁);
        }
    }

    @Test
    @DisplayName("connection 존재 여부를 반환한다.")
    void runIsExists() {
        assertThat(multiSinkHandler.isExists(현준)).isFalse();

        multiSinkHandler.create(현준);
        assertThat(multiSinkHandler.isExists(현준)).isTrue();
    }

    @Test
    @DisplayName("timeout을 반환한다.")
    void runTimeOut() {
        final Duration timeout = multiSinkHandler.timeout();

        assertThat(timeout).isNotNull();
    }

    @Test
    @DisplayName("key가 null일 경우 예외를 반환한다")
    void throwExceptionIfKeyNull() {
        assertAll(
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.connect(null).blockLast())
                                .isInstanceOf(InvalidSinksKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.sendEvent(null, "test"))
                                .isInstanceOf(InvalidSinksKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.complete(null))
                                .isInstanceOf(InvalidSinksKeyException.class),
                () ->
                        assertThatThrownBy(
                                () -> multiSinkHandler.create(null))
                                .isInstanceOf(InvalidSinksKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.getSink(null))
                                .isInstanceOf(InvalidSinksKeyException.class));
    }
}