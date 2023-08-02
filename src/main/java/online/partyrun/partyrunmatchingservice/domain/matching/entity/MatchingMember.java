package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidMatchingIdException;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MatchingMember {
    String id;
    MatchingMemberStatus status = MatchingMemberStatus.NO_RESPONSE;

    public MatchingMember(final String id) {
        validateId(id);
        this.id = id;
    }

    private void validateId(String id) {
        if (Objects.isNull(id) || id.isEmpty()) {
            throw new InvalidMatchingIdException(id);
        }
    }

    public void changeStatus(final MatchingMemberStatus status) {
        this.status = status;
    }

    public boolean isCanceled() {
        return status == MatchingMemberStatus.CANCELED;
    }

    public boolean isReady() {
        return status == MatchingMemberStatus.READY;
    }
}
