package online.partyrun.partyrunmatchingservice.domain.battle.service.external;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import online.partyrun.jwtmanager.JwtGenerator;
import online.partyrun.partyrunmatchingservice.domain.battle.service.BattleService;
import online.partyrun.partyrunmatchingservice.domain.battle.service.external.dto.BattleResponse;
import online.partyrun.partyrunmatchingservice.domain.battle.service.external.dto.CreateBattleRequest;
import online.partyrun.partyrunmatchingservice.domain.battle.service.external.dto.IsRunningResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExternalBattleService implements BattleService {
    WebClient battleClient;
    String systemToken;

    public ExternalBattleService(
            @Value("${external.battle.url}") String battleUrl, JwtGenerator jwtGenerator) {
        this.battleClient = WebClient.create(battleUrl);
        this.systemToken =
                jwtGenerator.generate("MATCHING_SERVICE", Set.of("ROLE_SYSTEM")).accessToken();
    }

    @Override
    public Mono<String> create(List<String> memberIds, int distance) {

        return battleClient
                .post()
                .uri("/api/battles")
                .header("Authorization", systemToken)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateBattleRequest(memberIds, distance))
                .retrieve()
                .bodyToMono(BattleResponse.class)
                .map(BattleResponse::id);
    }

    @Override
    public Mono<Boolean> isRunning(final String memberId) {
        return battleClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/api/battles/runners/{id}/is-running")
                                        .build(memberId))
                .header("Authorization", systemToken)
                .retrieve()
                .bodyToMono(IsRunningResponse.class)
                .map(IsRunningResponse::isRunning);
    }
}
