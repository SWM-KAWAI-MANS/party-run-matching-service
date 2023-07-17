package online.partyrun.partyrunmatchingservice.domain.match.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class InvalidIdException extends BadRequestException {
    public InvalidIdException() {
        super("Id값이 올바르지 않습니다.");
    }
}
