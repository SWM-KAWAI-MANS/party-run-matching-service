package online.partyrun.partyrunmatchingservice.domain.waiting.root;

public record WaitingMember(String memberId, RunningDistance distance) {
    public WaitingMember(String memberId, int distance) {
        this(memberId, RunningDistance.getBy(distance));
    }
}
