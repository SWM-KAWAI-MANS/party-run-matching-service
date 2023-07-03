package online.partyrun.application.domain.match.service;

import online.partyrun.application.domain.match.domain.MatchEvent;
import online.partyrun.application.global.handler.MultiSinkHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

/**
 * {@link MatchEvent}를 제공하는 {@link Sinks} 를 구동, 관리하는 Handler입니다.
 *
 * @author Hyeonjun Park
 * @see MultiSinkHandler
 * @see online.partyrun.application.global.handler.ServerSentEventHandler
 * @since 2023-06-29
 */
@Component
public class MatchEventHandler extends MultiSinkHandler<String, MatchEvent> {
    /**
     * sink를 추가합니다. subscribe시에 해당 sink에 포함된 모든 event를 publish합니다.
     * 처음 추가시 {@link MatchEvent} 중 CONNECT EVENT를 추가합니다.
     * 만약 이미 존재하면 수행하지 않습니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public void addSink(final String key) {
        if (!isExists(key)) {
            putSink(key, Sinks.many().replay().all());
            getSink(key).tryEmitNext(MatchEvent.CONNECT).orThrow();
        }
    }
}
