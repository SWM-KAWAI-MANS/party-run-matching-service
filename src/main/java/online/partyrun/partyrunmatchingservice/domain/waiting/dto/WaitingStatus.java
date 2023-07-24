package online.partyrun.partyrunmatchingservice.domain.waiting.dto;

public enum WaitingStatus {
    CONNECTED,
    MATCHED;


    public boolean isCompleted() {
        return this.equals(MATCHED);
    }
}
