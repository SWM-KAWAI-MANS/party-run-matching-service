package online.partyrun.application.domain.waiting.dto;

import online.partyrun.application.domain.waiting.domain.WaitingEvent;

/**
 * waitEvent 응답시 client에 전달하는 record 입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
public record WaitingEventResponse(String status, String message) {
    public WaitingEventResponse(WaitingEvent event) {
        this(event.getStatus(), event.getMessage());
    }
}
