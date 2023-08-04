package online.partyrun.partyrunmatchingservice.domain.battle.external;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import online.partyrun.jwtmanager.JwtGenerator;
import online.partyrun.partyrunmatchingservice.domain.battle.BattleService;
import online.partyrun.partyrunmatchingservice.domain.battle.external.dto.BattleResponse;
import online.partyrun.partyrunmatchingservice.domain.battle.external.dto.CreateBattleRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExternalBattleService implements BattleService {
    WebClient battleClient;
    JwtGenerator jwtGenerator;

    public ExternalBattleService(
            @Value("${external.battle.url}") String battleUrl, JwtGenerator jwtGenerator) {
        this.battleClient = WebClient.create(battleUrl);
        this.jwtGenerator = jwtGenerator;
    }

    @Override
    public Mono<String> create(List<String> memberIds, int distance) {
        log.info("EBS 호출");
        final String systemToken =
                jwtGenerator.generate("MATCHING_SERVICE", Set.of("ROLE_SYSTEM")).accessToken();
        return battleClient
                .post()
                .uri("/api/battles")
                .header("Authorization", systemToken)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateBattleRequest(memberIds, distance))
                .retrieve()
                .bodyToMono(BattleResponse.class)
                .log()
                .map(BattleResponse::id);
    }
}
