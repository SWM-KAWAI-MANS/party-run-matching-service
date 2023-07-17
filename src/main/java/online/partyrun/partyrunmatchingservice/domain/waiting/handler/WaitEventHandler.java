package online.partyrun.partyrunmatchingservice.domain.waiting.handler;

import online.partyrun.partyrunmatchingservice.domain.waiting.domain.WaitingEvent;
import online.partyrun.partyrunmatchingservice.global.handler.MultiSinkHandler;
import online.partyrun.partyrunmatchingservice.global.handler.ServerSentEventHandler;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * {@link WaitingEvent}를 제공하는 {@link Sinks} 를 구동, 관리하는 Handler입니다.
 *
 * @author Hyeonjun Park
 * @see MultiSinkHandler
 * @see ServerSentEventHandler
 * @since 2023-06-29
 */
@Component
public class WaitEventHandler extends MultiSinkHandler<String, WaitingEvent> {

    /**
     * sink를 추가합니다. subscribe시에 해당 sink에 포함된 모든 event를 publish합니다. 처음 추가시 {@link WaitingEvent} 중
     * CONNECT EVENT를 추가합니다.
     *
     * @author Hyeonjun Park
     * @since 2023-06-29
     */
    @Override
    public void addSink(final String key) {
        putSink(key, Sinks.many().replay().all());
        getSink(key).tryEmitNext(WaitingEvent.CONNECT);
    }

    /**
     * sink의 timeout를 설정합니다. 현재 30분으로 설정하였습니다
     *
     * @author Hyeonjun Park
     * @see MultiSinkHandler
     * @since 2023-06-29
     */
    @Override
    protected Duration timeout() {
        return Duration.ofMinutes(30);
    }
}