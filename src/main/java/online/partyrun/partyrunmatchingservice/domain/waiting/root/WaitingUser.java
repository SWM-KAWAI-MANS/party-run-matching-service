package online.partyrun.partyrunmatchingservice.domain.waiting.root;

public record WaitingUser(String userId, RunningDistance distance) {
    public WaitingUser(String userId, int distance) {
        this(userId, RunningDistance.getByMeter(distance));
    }
}
