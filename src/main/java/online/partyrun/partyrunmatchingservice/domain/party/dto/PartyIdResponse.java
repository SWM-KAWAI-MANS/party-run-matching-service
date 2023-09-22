package online.partyrun.partyrunmatchingservice.domain.party.dto;

import online.partyrun.partyrunmatchingservice.domain.party.entity.Party;

public record PartyIdResponse(String code) {
    public PartyIdResponse(Party party) {
        this(party.getEntryCode().getCode());
    }
}
