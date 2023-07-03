package online.partyrun.application.global.redis;

import online.partyrun.application.global.Exception.InternalServerException;

/**
 * Redis 관련 로직 수행 중 예외 발생시 사용합니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
public class RedisOperationException extends InternalServerException {
    public RedisOperationException() {
        super("redis 실행중 문제가 발생했습니다.");
    }
}
