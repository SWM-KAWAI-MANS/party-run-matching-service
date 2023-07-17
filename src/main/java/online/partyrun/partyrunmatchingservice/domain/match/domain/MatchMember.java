package online.partyrun.partyrunmatchingservice.domain.match.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.match.exception.InvalidIdException;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MatchMember {
    String id;
    MemberStatus status = MemberStatus.NO_RESPONSE;

    public MatchMember(final String id) {
        validateId(id);
        this.id = id;
    }

    private void validateId(String id) {
        if(Objects.isNull(id) || id.isEmpty()) {
            throw new InvalidIdException();
        }
    }

    public void reddy() {
        status = MemberStatus.READY;
    }

    public void cancel() {
        status = MemberStatus.CANCELED;
    }
}
