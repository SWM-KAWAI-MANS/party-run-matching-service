package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.config.IntegrationTest;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@IntegrationTest
@DisplayName("WaitingCheckService")
class WaitingCheckServiceTest {

    @Autowired WaitingCheckService waitingCheckService;
    @Test
    @DisplayName("queue가 비어있을 경우 Mono.Empty를 반환한다")
    void returnMonoEmpty() {
        final Mono<Matching> result = waitingCheckService.check(RunningDistance.M1000);
         StepVerifier.create(result).verifyComplete();
    }
}