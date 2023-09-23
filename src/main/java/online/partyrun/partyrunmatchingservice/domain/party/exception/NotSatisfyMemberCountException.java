package online.partyrun.partyrunmatchingservice.domain.party.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class NotSatisfyMemberCountException extends BadRequestException {
    public NotSatisfyMemberCountException(final String code, final int size) {
        super(code + " 파티가 시작할 수 인원이 부족합니다. 현재 인원 수 :" + size);
    }
}
