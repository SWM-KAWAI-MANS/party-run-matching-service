package online.partyrun.partyrunmatchingservice.domain.matching.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class InvalidIdException extends BadRequestException {
    public InvalidIdException(String id) {
        super(id + "는 잘못된 ID입니다.");
    }
}
