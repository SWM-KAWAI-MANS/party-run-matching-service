package online.partyrun.application.domain.match.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.UUID;

/**
 * 매칭이 정해질 때 생성되는 도메인입니다.
 * redis 환경에서 관리하도록 설계했습니다.
 * id는 hash값으로 자동생성됩니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@RedisHash("match")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {
    @Id
    String id;
    int distance;

    public Match(final int distance) {
        this.id = UUID.randomUUID().toString();
        this.distance = distance;
    }
}
