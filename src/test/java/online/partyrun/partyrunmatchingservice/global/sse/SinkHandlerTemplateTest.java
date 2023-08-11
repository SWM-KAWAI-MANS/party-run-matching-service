package online.partyrun.partyrunmatchingservice.global.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import online.partyrun.partyrunmatchingservice.global.sse.exception.KeyNotExistException;
import online.partyrun.partyrunmatchingservice.global.sse.exception.NullKeyException;

import org.junit.jupiter.api.*;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("MultiSinkHandler")
class SinkHandlerTemplateTest {
    SinkHandlerTemplate<String, String> sinkHandlerTemplate = new SinkHandlerTemplate<>() {};

    final String 현준 = "현준";
    final String 성우 = "성우";
    final String 준혁 = "준혁";

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 한_명에_대한_싱크를_생셩했을_때 {

        @BeforeEach
        public void setUp() {
            sinkHandlerTemplate.create(현준);
        }

        @Test
        @DisplayName("sink를 추가한 다음 connect를 수행한 후 완료를 진행한다.")
        void runConnect() {
            final Flux<String> connected = sinkHandlerTemplate.connect(현준);
            sinkHandlerTemplate.complete(현준);

            StepVerifier.create(connected).verifyComplete();
        }

        @Test
        @DisplayName("event 추가를 수행한다")
        void runAddEvent() {
            final String event1 = "이벤트 시작";
            final String event2 = "이벤트 마지막";
            final Flux<String> connected = sinkHandlerTemplate.connect(현준);
            sinkHandlerTemplate.sendEvent(현준, event1);
            sinkHandlerTemplate.sendEvent(현준, event2);
            sinkHandlerTemplate.complete(현준);

            StepVerifier.create(connected).expectNext(event1, event2).verifyComplete();
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 여러_명이_생성되었을_때 {
        @BeforeEach
        void setup() {
            sinkHandlerTemplate.create(현준);
            sinkHandlerTemplate.create(성우);
            sinkHandlerTemplate.create(준혁);
        }

        @Test
        @DisplayName("셧다운 진행 시 모든 sink 완료시키고 sink map을 clear한다")
        void runShutDown() {

            final Flux<String> connect = sinkHandlerTemplate.connect(현준);

            sinkHandlerTemplate.shutdown();

            assertThat(sinkHandlerTemplate.getConnectors()).isEmpty();
            StepVerifier.create(connect).verifyComplete();
        }

        @Test
        @DisplayName("key 전체 조회를 수행한다.")
        void runGetConnectors() {

            assertThat(sinkHandlerTemplate.getConnectors()).containsExactlyInAnyOrder(현준, 성우, 준혁);
        }
    }

    @Test
    @DisplayName("조회시에 map에 key가 존재하지 않으면 예외를 처리한다")
    void throwIfKeyNotExist() {
        assertThatThrownBy(() -> sinkHandlerTemplate.complete("nullkey"))
                .isInstanceOf(KeyNotExistException.class);
    }

    @Test
    @DisplayName("key가 null일 경우 예외를 반환한다")
    void throwExceptionIfKeyNull() {
        assertAll(
                () ->
                        assertThatThrownBy(() -> sinkHandlerTemplate.connect(null).blockLast())
                                .isInstanceOf(NullKeyException.class),
                () ->
                        assertThatThrownBy(() -> sinkHandlerTemplate.sendEvent(null, "test"))
                                .isInstanceOf(NullKeyException.class),
                () ->
                        assertThatThrownBy(() -> sinkHandlerTemplate.complete(null))
                                .isInstanceOf(NullKeyException.class),
                () ->
                        assertThatThrownBy(() -> sinkHandlerTemplate.create(null))
                                .isInstanceOf(NullKeyException.class));
    }

    @Test
    @DisplayName("해당하는 key가 만약 존재한다면 disconnect를 진행한다")
    void runDisconnect() {
        sinkHandlerTemplate.create(현준);

        sinkHandlerTemplate.disconnectIfExist(현준);

        assertThat(sinkHandlerTemplate.getConnectors()).isEmpty();
    }
}
