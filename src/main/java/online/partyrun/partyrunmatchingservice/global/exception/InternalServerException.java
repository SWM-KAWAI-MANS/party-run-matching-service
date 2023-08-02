package online.partyrun.partyrunmatchingservice.global.exception;

public abstract class InternalServerException extends RuntimeException {
    protected InternalServerException() {
        super("알 수 없는 에러입니다.");
    }

    protected InternalServerException(String message) {
        super(message);
    }
}
