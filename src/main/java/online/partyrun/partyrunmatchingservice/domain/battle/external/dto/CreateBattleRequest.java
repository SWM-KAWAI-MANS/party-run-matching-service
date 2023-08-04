package online.partyrun.partyrunmatchingservice.domain.battle.external.dto;

import java.util.List;

public record CreateBattleRequest(List<String> runnerIds, int distance) {
}
