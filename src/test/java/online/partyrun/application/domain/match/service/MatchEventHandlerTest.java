package online.partyrun.application.domain.match.service;

import online.partyrun.application.domain.match.domain.MatchEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MatchEventHandler")

class MatchEventHandlerTest {
    MatchEventHandler matchEventHandler = new MatchEventHandler();

    @Test
    @DisplayName("sink 추가시에 CONNECT Event를 추가한다")
    void runAddSinkAndAddEvent() {
        final String key = "hello";
        matchEventHandler.addSink(key);

        final Flux<MatchEvent> connect = matchEventHandler.connect(key);
        matchEventHandler.complete(key);

         StepVerifier.create(connect)
                         .assertNext(res ->  assertThat(res).isEqualTo(MatchEvent.CONNECT))
                         .verifyComplete();
    }
}