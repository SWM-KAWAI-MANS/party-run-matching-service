package online.partyrun.partyrunmatchingservice.domain.match.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class InvalidDistanceException extends BadRequestException {
    public InvalidDistanceException() {
        super("거리값이 올바르지 않습니다.");
    }
}
