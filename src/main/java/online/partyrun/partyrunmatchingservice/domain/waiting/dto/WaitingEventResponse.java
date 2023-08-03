package online.partyrun.partyrunmatchingservice.domain.waiting.dto;

public record WaitingEventResponse(String status) {
    public WaitingEventResponse(WaitingStatus status) {
        this(status.name());
    }
}
