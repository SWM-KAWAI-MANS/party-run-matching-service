package online.partyrun.application.domain.match.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * match를 진행하는 runner 도메인입니다.
 * redis 환경에서 관리하도록 설계했습니다.
 * id는 hash값으로 자동생성됩니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RedisHash("runner")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Runner {
    @Id
    String memberId;
    String matchId;
    RunnerStatus status;

    public void changeStatus(RunnerStatus status) {
        this.status = status;
    }
}
