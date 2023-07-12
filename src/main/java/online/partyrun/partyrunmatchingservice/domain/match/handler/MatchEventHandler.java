package online.partyrun.partyrunmatchingservice.domain.match.handler;

import online.partyrun.partyrunmatchingservice.domain.match.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.global.handler.MultiSinkHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class MatchEventHandler extends MultiSinkHandler<String, MatchEvent> {
    @Override
    public void addSink(final String key) {
        putSink(key, Sinks.many().replay().all());
    }
}
