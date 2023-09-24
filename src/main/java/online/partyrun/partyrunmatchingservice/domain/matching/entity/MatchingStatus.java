package online.partyrun.partyrunmatchingservice.domain.matching.entity;

public enum MatchingStatus {
    WAIT,
    SUCCESS,
    CANCEL;

    public boolean isWait() {
        return this == WAIT;
    }
}
