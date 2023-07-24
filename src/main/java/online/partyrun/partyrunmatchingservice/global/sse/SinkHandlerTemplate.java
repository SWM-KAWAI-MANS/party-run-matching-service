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
public abstract class SinkHandlerTemplate<K, V> implements ServerSentEventHandler<K, V> {
    private static final int DEFAULT_MINUTE = 3;
    Map<K, Sinks.Many<V>> sinks = new ConcurrentHashMap<>();

    @Override
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

    @Override
    public void sendEvent(final K key, final V value) {
        getSink(key).tryEmitNext(value);
    }

    @Override
    public void complete(final K key) {
        getSink(key).tryEmitComplete();
        sinks.remove(key);
    }

    @Override
    public void create(K key) {
        checkKeyNotNull(key);
        sinks.putIfAbsent(key, Sinks.many().unicast().onBackpressureBuffer());
    }

    private Sinks.Many<V> getSink(K key) {
        validateKey(key);
        return sinks.get(key);
    }

    private void validateKey(K key) {
        checkKeyNotNull(key);
        if (isNonExists(key)) {
            throw new KeyNotExistException(key.toString());
        }
    }

    private void checkKeyNotNull(K key) {
        if (Objects.isNull(key)) {
            throw new NullKeyException();
        }
    }

    private boolean isNonExists(K key) {
        return !sinks.containsKey(key);
    }

    private Duration timeout() {
        return Duration.ofMinutes(DEFAULT_MINUTE);
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

    @Override
    public void disconnectIfExist(final K key) {
        if (sinks.containsKey(key)) {
            complete(key);
        }
    }


}
