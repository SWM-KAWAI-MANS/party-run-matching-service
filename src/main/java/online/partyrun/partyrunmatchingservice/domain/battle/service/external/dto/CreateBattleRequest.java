package online.partyrun.partyrunmatchingservice.domain.battle.service.external.dto;

import java.util.List;

public record CreateBattleRequest(List<String> runnerIds, int distance) {
}
