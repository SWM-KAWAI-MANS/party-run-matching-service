package online.partyrun.partyrunmatchingservice.global.sse.exception;

public class InvalidSinksKeyException extends IllegalArgumentException {
    public InvalidSinksKeyException() {
        super("sinks의 key값이 올바르지 않습니다.");
    }
}
