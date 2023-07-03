package online.partyrun.application.domain.match.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Match 진행시 발생하는 Event 열거형 클래스입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum MatchEvent {
    CONNECT("연결되었습니다."),
    WAIT("대기중입니다."),
    COMPLETE("매치 준비가 완료되었습니다."),
    CANCEL("매치가 취소되었습니다"),
    TIMEOUT("요청 시간이 초과되었습니다");

    String message;
}
