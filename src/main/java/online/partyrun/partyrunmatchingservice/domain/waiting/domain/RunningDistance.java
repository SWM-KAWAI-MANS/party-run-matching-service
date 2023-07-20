package online.partyrun.partyrunmatchingservice.domain.waiting.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * running 거리 열거형 클래스 입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum RunningDistance {
    M1000(1000),
    M3000(3000),
    M5000(5000),
    M10000(10000);

    int meter;

    // TODO int to RunningDistance 메서드 생성
}
