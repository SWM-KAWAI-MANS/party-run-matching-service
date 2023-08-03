package online.partyrun.partyrunmatchingservice.global.exception;

public class InvalidSinksKeyException extends BadRequestException {
    public InvalidSinksKeyException() {
        super("sinks의 key값이 올바르지 않습니다.");
    }
}
