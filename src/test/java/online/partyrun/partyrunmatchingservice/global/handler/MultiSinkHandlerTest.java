package online.partyrun.partyrunmatchingservice.global.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import online.partyrun.partyrunmatchingservice.global.exception.InvalidSinksKeyException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;

@DisplayName("MultiSinkHandler")
class MultiSinkHandlerTest {
    MultiSinkHandler<String, String> multiSinkHandler =
            new MultiSinkHandler<>() {
                @Override
                public void addSink(final String key) {
                    putSink(key, Sinks.many().replay().all());
                }
            };

    final String key = "sample";

    @Test
    @DisplayName("sink를 추가한 다음 connect를 수행한 후 완료를 진행한다.")
    void runConnect() {
        multiSinkHandler.addSink(key);
        final Flux<String> connected = multiSinkHandler.connect(key);
        multiSinkHandler.complete(key);

        StepVerifier.create(connected).verifyComplete();
    }

    @Test
    @DisplayName("event 추가를 수행한다")
    void runAddEvent() {
        String event1 = "hello";
        String event2 = "hello2";
        multiSinkHandler.addSink(key);
        final Flux<String> connected = multiSinkHandler.connect(key);
        multiSinkHandler.sendEvent(key, event1);
        multiSinkHandler.sendEvent(key, event2);
        multiSinkHandler.complete(key);

        StepVerifier.create(connected).expectNext(event1, event2).verifyComplete();
    }

    @Test
    @DisplayName("sink 가져오기를 수행한다")
    void runGetSink() {
        multiSinkHandler.addSink(key);
        final Sinks.Many<String> sink = multiSinkHandler.getSink(key);
        sink.tryEmitComplete();

        StepVerifier.create(sink.asFlux()).expectNextCount(0).verifyComplete();
    }

    @Test
    @DisplayName("connection 존재 여부를 반환한다.")
    void runIsExists() {
        assertThat(multiSinkHandler.isExists(key)).isFalse();

        multiSinkHandler.addSink(key);
        assertThat(multiSinkHandler.isExists(key)).isTrue();
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
                        assertThatThrownBy(() -> multiSinkHandler.connect(null))
                                .isInstanceOf(InvalidSinksKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.sendEvent(null, "test"))
                                .isInstanceOf(InvalidSinksKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.complete(null))
                                .isInstanceOf(InvalidSinksKeyException.class),
                () ->
                        assertThatThrownBy(
                                        () ->
                                                multiSinkHandler.putSink(
                                                        null, Sinks.many().replay().all()))
                                .isInstanceOf(InvalidSinksKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.getSink(null))
                                .isInstanceOf(InvalidSinksKeyException.class),
                () ->
                        assertThatThrownBy(() -> multiSinkHandler.isExists(null))
                                .isInstanceOf(InvalidSinksKeyException.class));
    }
}
