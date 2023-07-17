package online.partyrun.partyrunmatchingservice.domain.waiting.dto;

import jakarta.validation.constraints.NotNull;
import online.partyrun.partyrunmatchingservice.domain.waiting.domain.RunningDistance;

/**
 * waiting 생성시 필요한 요청 record 입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
public record CreateWaitingRequest(@NotNull RunningDistance distance) {}
