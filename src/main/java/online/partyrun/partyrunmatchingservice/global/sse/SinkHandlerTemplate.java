package online.partyrun.partyrunmatchingservice.global.sse;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.global.sse.exception.KeyNotExistException;
import online.partyrun.partyrunmatchingservice.global.sse.exception.NullKeyException;
import online.partyrun.partyrunmatchingservice.global.sse.exception.SseConnectionException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class SinkHandlerTemplate<K, V> {
    private static final int DEFAULT_MINUTE = 3;
    Map<K, Sinks.Many<V>> sinks = new ConcurrentHashMap<>();

    public Flux<V> connect(final K key) {
        return getSink(key)
                .asFlux()
                .timeout(timeout())
                .doOnCancel(() -> sinks.remove(key))
                .doOnError(
                        e -> {
                            sinks.remove(key);
                            throw new SseConnectionException();
                        });
    }

    public void create(K key) {
        checkKeyNotNull(key);
        sinks.putIfAbsent(key, Sinks.many().unicast().onBackpressureBuffer());
    }

    private void checkKeyNotNull(K key) {
        if (Objects.isNull(key)) {
            throw new NullKeyException();
        }
    }

    public void sendEvent(final K key, final V value) {
        getSink(key).tryEmitNext(value);
    }

    public void complete(final K key) {
        getSink(key).tryEmitComplete();
        sinks.remove(key);
    }

    public void shutdown() {
        sinks.keySet().forEach(key -> sinks.get(key).tryEmitComplete());
        sinks.clear();
    }

    private Sinks.Many<V> getSink(K key) {
        validateKey(key);
        return sinks.get(key);
    }

    private void validateKey(K key) {
        checkKeyNotNull(key);
        if (!isExist(key)) {
            throw new KeyNotExistException(key.toString());
        }
    }

    public boolean isExist(K key) {
        return sinks.containsKey(key);
    }

    protected Duration timeout() {
        return Duration.ofMinutes(DEFAULT_MINUTE);
    }

    public List<K> getConnectors() {
        return sinks.keySet().stream().toList();
    }

    public void disconnectIfExist(final K key) {
        if (isExist(key)) {
            complete(key);
        }
    }
}
