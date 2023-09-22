package online.partyrun.partyrunmatchingservice.domain.party.dto;

import online.partyrun.partyrunmatchingservice.domain.party.entity.Party;
import online.partyrun.partyrunmatchingservice.domain.party.entity.PartyStatus;

import java.util.List;

public record PartyEvent(String entryCode,
                         int distance, String leaderId,
                         PartyStatus status,
                         List<String> participants,
                         String battleId) {
    public PartyEvent(Party party) {
        this(party.getEntryCode().getCode(), party.getDistance().getMeter(), party.getManagerId(),
                party.getStatus(), party.getParticipants(), party.getBattleId());
    }
}
