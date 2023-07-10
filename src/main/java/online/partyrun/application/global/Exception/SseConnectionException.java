package online.partyrun.application.global.Exception;

/**
 * Sse connection 진행 중 애러 발생시 사용하는 exception 입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
public class SseConnectionException extends InternalServerException {
    public SseConnectionException() {
        super("Sse 연결 중 문제가 발생했습니다.");
    }
}
