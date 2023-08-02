package online.partyrun.partyrunmatchingservice.domain.matching.exception;

import online.partyrun.partyrunmatchingservice.global.exception.BadRequestException;

public class NotExistMembersException extends BadRequestException {
    public NotExistMembersException() {
        super("참여자 정보가 비어있습니다");
    }
}
