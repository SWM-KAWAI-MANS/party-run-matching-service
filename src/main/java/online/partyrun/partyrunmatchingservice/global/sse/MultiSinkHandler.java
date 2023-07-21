package online.partyrun.partyrunmatchingservice.global.sse;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.global.sse.exception.InvalidSinksKeyException;
import online.partyrun.partyrunmatchingservice.global.sse.exception.SseConnectionException;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MultiSinkHandler<K, V> implements ServerSentEventHandler<K, V> {
    Map<K, Sinks.Many<V>> sinks = new HashMap<>();

    @Override
    public Flux<V> connect(final K key) {
        return getSink(key)
                .asFlux()
                .subscribeOn(Schedulers.boundedElastic())
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
        validateKey(key);
        sinks.putIfAbsent(key, Sinks.many().replay().all());
    }

    protected Sinks.Many<V> getSink(K key) {
        validateKey(key);
        return sinks.get(key);
    }

    private void validateKey(K key) {
        if (Objects.isNull(key)) {
            throw new InvalidSinksKeyException();
        }
    }

    protected boolean isExists(K key) {
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
