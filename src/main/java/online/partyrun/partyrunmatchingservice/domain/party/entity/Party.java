package online.partyrun.partyrunmatchingservice.domain.party.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Party {
    @Id
    String id;
    EntryCode entryCode = new EntryCode();
    String managerId;
    RunningDistance distance;

    PartyStatus status = PartyStatus.WAITING;
    List<String> participants = new ArrayList<>();
    String battleId;

    public Party(String managerId, RunningDistance distance) {
        this.managerId = managerId;
        this.distance = distance;
    }

    public void join(String memberId) {
        participants.add(memberId);
    }


    public void start(String battleId) {
        this.battleId = battleId;
        status = PartyStatus.COMPLETED;
    }
}
