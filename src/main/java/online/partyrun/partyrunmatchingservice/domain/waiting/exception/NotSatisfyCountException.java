package online.partyrun.partyrunmatchingservice.domain.waiting.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class NotSatisfyCountException extends BadRequestException {
    public NotSatisfyCountException() {
        super("적정 유저 수가 충족하지 않았습니다.");
    }
}
