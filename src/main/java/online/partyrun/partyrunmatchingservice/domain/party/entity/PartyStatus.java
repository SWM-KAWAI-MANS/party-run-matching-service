package online.partyrun.partyrunmatchingservice.domain.party.entity;

public enum PartyStatus {
    WAITING, COMPLETED, CANCELLED;

    public boolean isWaiting() {
        return this.equals(WAITING);
    }
}
