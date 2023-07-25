package online.partyrun.partyrunmatchingservice.domain.matching.entity;

public enum MatchingMemberStatus {
    NO_RESPONSE,
    READY,
    CANCELED;

    public static MatchingMemberStatus getByIsJoin(boolean isJoin) {
        if(isJoin) {
            return READY;
        }
        return CANCELED;
    }
}
