package online.partyrun.partyrunmatchingservice.domain.party.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class PartyClosedException extends BadRequestException {
    public PartyClosedException(final String code) {
        super(code + " 파티는 이미 마감되었습니다.");
    }
}
