package online.partyrun.partyrunmatchingservice.domain.waiting.exception;

public class NotAllowMeterException extends IllegalArgumentException {
    public NotAllowMeterException(int meter) {
        super(meter + "는 허용하지 않은 거리입니다.");
    }
}
