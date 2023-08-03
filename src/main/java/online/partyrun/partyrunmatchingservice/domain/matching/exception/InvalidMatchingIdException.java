package online.partyrun.partyrunmatchingservice.domain.matching.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class InvalidMatchingIdException extends BadRequestException {
    public InvalidMatchingIdException(String id) {
        super(id + "는 잘못된 ID입니다.");
    }
}
