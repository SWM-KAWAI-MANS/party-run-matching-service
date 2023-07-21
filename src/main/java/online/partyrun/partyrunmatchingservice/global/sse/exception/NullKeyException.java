package online.partyrun.partyrunmatchingservice.global.sse.exception;

public class NullKeyException extends IllegalArgumentException {
    public NullKeyException() {
        super("key 값이 null입니다.");
    }
}
