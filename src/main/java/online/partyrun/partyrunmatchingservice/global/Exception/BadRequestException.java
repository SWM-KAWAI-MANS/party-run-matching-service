package online.partyrun.partyrunmatchingservice.global.Exception;

public abstract class BadRequestException extends RuntimeException {
    public BadRequestException() {
        super("잘못된 요청입니다.");
    }

    public BadRequestException(String message) {
        super(message);
    }
}
