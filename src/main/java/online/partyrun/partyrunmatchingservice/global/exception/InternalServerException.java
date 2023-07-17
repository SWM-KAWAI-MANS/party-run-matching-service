package online.partyrun.partyrunmatchingservice.global.exception;

/**
 * 알 수 없는 에러가 발생시 사용하는 Exception 클래스입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
public abstract class InternalServerException extends RuntimeException {
    protected InternalServerException() {
        super("알 수 없는 에러입니다.");
    }

    protected InternalServerException(String message) {
        super(message);
    }
}
