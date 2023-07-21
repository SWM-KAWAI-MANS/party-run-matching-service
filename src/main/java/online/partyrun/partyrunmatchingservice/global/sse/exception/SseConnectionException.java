package online.partyrun.partyrunmatchingservice.global.sse.exception;

public class SseConnectionException extends RuntimeException {
    public SseConnectionException() {
        super("Sse 연결 중 문제가 발생했습니다.");
    }
}
