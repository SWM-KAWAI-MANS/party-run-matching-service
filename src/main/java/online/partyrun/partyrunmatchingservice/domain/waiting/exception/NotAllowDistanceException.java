package online.partyrun.partyrunmatchingservice.domain.waiting.exception;

public class NotAllowDistanceException extends IllegalArgumentException {
    public NotAllowDistanceException(int meter) {
        super(meter + "는 허용하지 않은 거리입니다.");
    }
}
