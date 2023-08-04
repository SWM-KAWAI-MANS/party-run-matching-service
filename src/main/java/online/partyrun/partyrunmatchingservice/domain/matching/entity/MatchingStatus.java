package online.partyrun.partyrunmatchingservice.domain.matching.entity;

public enum MatchingStatus {
    WAIT,
    SUCCESS,
    CANCEL;

    public boolean isWait() {
        return this == WAIT;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
