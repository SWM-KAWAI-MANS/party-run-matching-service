package online.partyrun.partyrunmatchingservice.domain.party.entity;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.security.SecureRandom;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class EntryCode {
    private static int MIN_RANDOM_NUMBER = 100_000;
    private static int MAX_RANDOM_NUMBER = 999_999;

    private String code;

    public EntryCode() {
        this.code = generateRandomRoomId();
    }

    private String generateRandomRoomId() {
        return String.valueOf(new SecureRandom().nextInt(MIN_RANDOM_NUMBER, MAX_RANDOM_NUMBER + 1));
    }
}
