package online.partyrun.partyrunmatchingservice.domain.party.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntryCodeTest {
    @Test
    @DisplayName("entryCode 생성 시 100,000 - 999,999 중 랜덤한 값으로 생성한다.")
    void constructRandom() {
        EntryCode code = new EntryCode();
        Integer value = Integer.parseInt(code.getCode());

        assertThat(value).isBetween(100_000, 999_999);
    }
}