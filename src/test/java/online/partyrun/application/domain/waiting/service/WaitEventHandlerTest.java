package online.partyrun.application.domain.waiting.service;

import online.partyrun.application.domain.waiting.handler.WaitEventHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WaitEventHandler")
class WaitEventHandlerTest {
    WaitEventHandler handler = new WaitEventHandler();

    String key = "sample";

    @Test
    @DisplayName("sink를 추가한다")
    void successAddSink() {
        handler.addSink(key);
    }
}
