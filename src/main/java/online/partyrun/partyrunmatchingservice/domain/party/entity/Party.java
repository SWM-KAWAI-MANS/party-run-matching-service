package online.partyrun.partyrunmatchingservice.domain.party.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.party.exception.IllegalArgumentIdException;
import online.partyrun.partyrunmatchingservice.domain.party.exception.NotSatisfyMemberCountException;
import online.partyrun.partyrunmatchingservice.domain.party.exception.PartyClosedException;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Party {
    private static final int MIN_PARTY_START_MEMBER_SIZE = 2;
    @Id
    String id;
    EntryCode entryCode = new EntryCode();
    String managerId;
    RunningDistance distance;

    PartyStatus status = PartyStatus.WAITING;
    Set<String> participantIds = new HashSet<>();
    String battleId;

    public Party(String managerId, RunningDistance distance) {
        validateArgumentId(managerId);
        this.managerId = managerId;
        this.distance = distance;
    }

    public void join(String memberId) {
        validateArgumentId(memberId);
        participantIds.add(memberId);
    }


    public void start(String battleId) {
        validateArgumentId(battleId);
        validatePartyState();
        this.battleId = battleId;
        status = PartyStatus.COMPLETED;
    }
    private void validatePartyState() {
        if(!status.isWaiting()) {
            throw new PartyClosedException(entryCode.getCode());
        }
        if(participantIds.size() < MIN_PARTY_START_MEMBER_SIZE) {
            throw new NotSatisfyMemberCountException(entryCode.getCode(), participantIds.size());
        }
    }

    public void quit(final String memberId) {
        validateArgumentId(memberId);
        participantIds.remove(memberId);
        if(memberId.equals(managerId)) {
            status = PartyStatus.CANCELLED;
        }
    }

    public boolean isRecruitClosed() {
        return !status.isWaiting();
    }
    private void validateArgumentId(String id) {
        if(Objects.isNull(id) || id.isBlank()) {
            throw new IllegalArgumentIdException(id);
        }
    }
}
