package online.partyrun.application.global.handler;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;
import online.partyrun.application.domain.waiting.exception.SseConnectionException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * MultiSink를 이용해서 {@link ServerSentEventHandler} 를 구현합니다. 각 sink는 Map을 통해서 관리합니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public abstract class MultiSinkHandler<K, V> implements ServerSentEventHandler<K, V> {
    Map<K, Sinks.Many<V>> sinks = new HashMap<>();

    /**
     * 주어진 key로 정해진 timeout 시간동안 connection을 진행합니다. connection 종료시에 map에서 제거합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public Flux<V> connect(final K key) {
        return getSink(key)
                .asFlux()
                .doOnCancel(() -> sinks.remove(key))
                .timeout(timeout())
                .doOnError(
                        e -> {
                            sinks.remove(key);
                            throw new SseConnectionException();
                        });
    }

    /**
     * 애당되는 key에 event를 추가합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public void sendEvent(final K key, final V value) {
        sinks.get(key).tryEmitNext(value);
    }

    /**
     * 주어진 key에 해당하는 sink를 완료합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public void complete(final K key) {
        sinks.get(key).tryEmitComplete();
    }

    @Override
    public void disconnect(final K key) {
        log.info("{}", sinks.keySet());
        sinks.remove(key);
    }

    /**
     * 새로운 sink를 추가합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    protected void putSink(K key, Sinks.Many<V> sink) {
        sinks.put(key, sink);
    }

    /**
     * 주어진 key에 해당하는 sink를 반환합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    protected Sinks.Many<V> getSink(K key) {
        return sinks.get(key);
    }

    /**
     * 해당 key가 존재하는지 여부를 파악합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    protected boolean isExists(K key) {
        return sinks.containsKey(key);
    }

    /**
     * timeout 시간을 가져옵니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    protected Duration timeout() {
        return Duration.ofMinutes(3);
    }
}
