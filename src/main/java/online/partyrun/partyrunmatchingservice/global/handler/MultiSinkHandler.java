package online.partyrun.partyrunmatchingservice.global.handler;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.global.exception.InvalidSinksKeyException;
import online.partyrun.partyrunmatchingservice.global.exception.SseConnectionException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class MultiSinkHandler<K, V> implements ServerSentEventHandler<K, V> {
    Map<K, Sinks.Many<V>> sinks = new HashMap<>();

    @Override
    public Flux<V> connect(final K key) {
        validateKey(key);
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

    private void validateKey(K key) {
        if (Objects.isNull(key)) {
            throw new InvalidSinksKeyException();
        }
    }

    /**
     * 애당되는 key에 event를 추가합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public void sendEvent(final K key, final V value) {
        validateKey(key);
        sinks.get(key).tryEmitNext(value);
    }

    @Override
    public void complete(final K key) {
        validateKey(key);
        sinks.get(key).tryEmitComplete();
        sinks.remove(key);
    }

    protected void putSink(K key, Sinks.Many<V> sink) {
        validateKey(key);
        sinks.computeIfAbsent(key, k -> sink);
    }

    protected Sinks.Many<V> getSink(K key) {
        validateKey(key);
        return sinks.get(key);
    }

    protected boolean isExists(K key) {
        validateKey(key);
        return sinks.containsKey(key);
    }

    protected Duration timeout() {
        return Duration.ofMinutes(3);
    }

    @Override
    public void shutdown() {
        sinks.keySet().forEach(key -> sinks.get(key).tryEmitComplete());
        sinks.clear();
    }

    @Override
    public List<K> getConnectors() {
        return sinks.keySet().stream().toList();
    }
}
