package online.partyrun.partyrunmatchingservice.domain.battle.service;


import reactor.core.publisher.Mono;

import java.util.List;
public interface BattleService {
    Mono<String> create(List<String> memberIds, int distance);
}

