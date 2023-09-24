package online.partyrun.partyrunmatchingservice.domain.party.dto;

import online.partyrun.partyrunmatchingservice.domain.party.entity.Party;
import online.partyrun.partyrunmatchingservice.domain.party.entity.PartyStatus;

import java.util.Set;

public record PartyEvent(String entryCode,
                         int distance,
                         String managerId,
                         PartyStatus status,
                         Set<String> participantIds,
                         String battleId) {
    public PartyEvent(Party party) {
        this(party.getEntryCode().getCode(), party.getDistance().getMeter(), party.getManagerId(),
                party.getStatus(), party.getParticipantIds(), party.getBattleId());
    }
}
