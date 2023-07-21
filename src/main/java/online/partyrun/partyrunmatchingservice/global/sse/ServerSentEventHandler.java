package online.partyrun.partyrunmatchingservice.global.sse;

import reactor.core.publisher.Flux;

import java.util.List;

public interface ServerSentEventHandler<K, V> {
    Flux<V> connect(K key);

    void create(K key);

    void sendEvent(K key, V value);

    void complete(K key);

    void shutdown();

    List<K> getConnectors();
}
