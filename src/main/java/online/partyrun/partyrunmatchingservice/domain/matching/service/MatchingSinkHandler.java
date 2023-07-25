package online.partyrun.partyrunmatchingservice.domain.matching.service;

import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.global.sse.SinkHandlerTemplate;

import org.springframework.stereotype.Component;

@Component
public class MatchingSinkHandler extends SinkHandlerTemplate<String, MatchEvent> {}
