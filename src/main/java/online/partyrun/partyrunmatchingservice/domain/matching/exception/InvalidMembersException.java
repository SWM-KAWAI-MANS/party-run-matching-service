package online.partyrun.partyrunmatchingservice.domain.matching.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class InvalidMembersException extends BadRequestException {
    public InvalidMembersException() {
        super("참여자 정보가 잘못되었습니다.");
    }
}
