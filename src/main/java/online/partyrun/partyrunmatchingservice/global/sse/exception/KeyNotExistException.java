package online.partyrun.partyrunmatchingservice.global.sse.exception;

public class KeyNotExistException extends IllegalArgumentException {
    public KeyNotExistException() {
        super("key가 존재하지 않습니다.");
    }
}
