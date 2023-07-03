package online.partyrun.application.domain.waiting.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * 대기시에 발생하는 이벤트 열거형 클래스 입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum WaitingEvent {
    CONNECT(false, "connection success"),
    MATCHED(true, "match completed"),
    TIMEOUT(false, "timeout");

    boolean isSuccess;
    String message;
}
