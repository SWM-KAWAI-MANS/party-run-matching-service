package online.partyrun.partyrunmatchingservice.domain.party.service;

import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyEvent;
import online.partyrun.partyrunmatchingservice.global.sse.SinkHandlerTemplate;
import org.springframework.stereotype.Component;

@Component
public class PartySinkHandler extends SinkHandlerTemplate<String, PartyEvent> {
}
