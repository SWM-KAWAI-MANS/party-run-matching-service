package online.partyrun.partyrunmatchingservice.domain.waiting.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class DuplicateMemberException extends BadRequestException {
    public DuplicateMemberException(String memberId) {
        super(memberId + "는 중복되었습니다.");
    }
}
