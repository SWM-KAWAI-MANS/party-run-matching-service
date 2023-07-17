package online.partyrun.partyrunmatchingservice.domain.match.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class InvalidMembersException extends BadRequestException {
    public InvalidMembersException() {
        super("참여자 목록이 올바르지 않습니다.");
    }
}
