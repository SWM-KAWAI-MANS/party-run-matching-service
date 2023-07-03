package online.partyrun.application.domain.match.dto;

import java.util.List;

/**
 * Battle 생성시 요구사항을 종합한 dto record 입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
public record CreateBattleRequest(List<String> runnerIds, int distance) {
}
