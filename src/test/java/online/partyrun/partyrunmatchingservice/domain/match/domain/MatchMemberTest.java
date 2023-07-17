package online.partyrun.partyrunmatchingservice.domain.match.domain;

import online.partyrun.partyrunmatchingservice.domain.match.exception.InvalidIdException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class MatchMemberTest {

    @Test
    @DisplayName("id값이 잘못 입력되면 예외를 반환하는가")
    void validateId() {
        assertAll(
                () ->  assertThatThrownBy(() -> new MatchMember(null))
                        .isInstanceOf(InvalidIdException.class),
                () ->  assertThatThrownBy(() -> new MatchMember(""))
                        .isInstanceOf(InvalidIdException.class)
        );
    }

}