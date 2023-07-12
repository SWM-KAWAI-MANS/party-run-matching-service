package online.partyrun.partyrunmatchingservice.domain.waiting.exception;

import online.partyrun.partyrunmatchingservice.global.Exception.BadRequestException;

public class DuplicateUserException extends BadRequestException {
    public DuplicateUserException() {
        super("중복된 유저를 추가하였습니다.");
    }
}
