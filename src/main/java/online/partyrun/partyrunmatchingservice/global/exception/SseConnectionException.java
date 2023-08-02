package online.partyrun.partyrunmatchingservice.global.exception;

public class SseConnectionException extends InternalServerException {
    public SseConnectionException() {
        super("Sse 연결 중 문제가 발생했습니다.");
    }
}
