package online.partyrun.partyrunmatchingservice.domain.party.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class IllegalArgumentIdException extends BadRequestException {
    public IllegalArgumentIdException(String id) {
        super(id + " 는 허용되지 않는 값입니다.");
    }
}
