package online.partyrun.partyrunmatchingservice.domain.waiting.dto;

public enum WaitingStatus {
    CONNECTED,
    MATCHED,
    CANCEL;

    public boolean isCompleted() {
        return !this.equals(CONNECTED);
    }
}
