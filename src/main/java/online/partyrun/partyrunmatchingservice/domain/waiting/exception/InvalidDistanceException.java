package online.partyrun.partyrunmatchingservice.domain.waiting.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class InvalidDistanceException extends BadRequestException {
    public InvalidDistanceException(int distance) {
        super(distance + " 는 허용하지 않는 거리입니다.");
    }
}
