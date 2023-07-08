package online.partyrun.application.domain.match.handler;

import online.partyrun.application.domain.match.dto.MatchEvent;
import online.partyrun.application.global.handler.MultiSinkHandler;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Sinks;

@Component
public class MatchEventHandler extends MultiSinkHandler<String, MatchEvent> {
    @Override
    public void addSink(final String key) {
        putSink(key, Sinks.many().replay().all());
    }
}
