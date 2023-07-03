package online.partyrun.application.domain.waiting.dto;

import online.partyrun.application.domain.waiting.domain.RunningDistance;

/**
 * waiting 생성시 필요한 요청 record 입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
public record CreateWaitingRequest(RunningDistance distance) {
}
