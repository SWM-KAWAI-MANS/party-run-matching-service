package online.partyrun.partyrunmatchingservice.domain.party.service;

import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyEvent;
import online.partyrun.partyrunmatchingservice.global.sse.SinkHandlerTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PartySinkHandler extends SinkHandlerTemplate<String, PartyEvent> {
    private static final int PARTY_TIME_OUT_MINUTES = 10;

    @Override
    protected Duration timeout() {
        return Duration.ofMinutes(PARTY_TIME_OUT_MINUTES);
    }
}
