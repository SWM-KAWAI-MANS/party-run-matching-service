package online.partyrun.partyrunmatchingservice.global.handler;

import reactor.core.publisher.Flux;

import java.util.List;

/**
 * ServerSentEvent handler 인터페이스입니다. connection 및 event를 관리합니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
public interface ServerSentEventHandler<K, V> {
    Flux<V> connect(K key);

    void addSink(K key);

    void sendEvent(K key, V value);

    void complete(K key);

    void shutdown();

    List<K> getConnectors();
}
