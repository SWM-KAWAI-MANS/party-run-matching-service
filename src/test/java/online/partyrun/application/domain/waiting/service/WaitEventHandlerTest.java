package online.partyrun.application.domain.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@DisplayName("WaitEventHandler")
class WaitEventHandlerTest {
    WaitEventHandler handler = new WaitEventHandler();

    String key = "sample";

    @Test
    @DisplayName("sink를 추가한다")
    void successAddSink() {
        handler.addSink(key);
    }

    @Test
    @DisplayName("timeout이 30분으로 설정된다")
    void successSetTimeout() {
        final Duration timeout = handler.timeout();

        assertThat(timeout.getSeconds()).isEqualTo(30 * 60);
    }
}
