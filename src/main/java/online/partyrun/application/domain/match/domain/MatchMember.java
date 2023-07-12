package online.partyrun.application.domain.match.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MatchMember {
    String id;
    MemberStatus status = MemberStatus.NO_RESPONSE;

    public MatchMember(final String id) {
        this.id = id;
    }

    public void reddy() {
        status = MemberStatus.READY;
    }

    public void cancel() {
        status = MemberStatus.CANCELED;
    }
}
