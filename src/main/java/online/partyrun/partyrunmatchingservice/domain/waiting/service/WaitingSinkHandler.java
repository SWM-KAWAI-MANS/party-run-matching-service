package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.global.sse.SinkHandlerTemplate;

import org.springframework.stereotype.Component;

@Component
public class WaitingSinkHandler extends SinkHandlerTemplate<String, WaitingStatus> {}
