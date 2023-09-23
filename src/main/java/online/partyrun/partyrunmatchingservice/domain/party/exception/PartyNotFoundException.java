package online.partyrun.partyrunmatchingservice.domain.party.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class PartyNotFoundException extends BadRequestException {
    public PartyNotFoundException(final String entryCode) {
        super(entryCode + "에 해당하는 Party를 찾을 수 없습니다.");
    }
}
