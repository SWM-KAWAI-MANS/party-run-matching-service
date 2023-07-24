package online.partyrun.partyrunmatchingservice.domain.matching.service;

import static org.assertj.core.api.Assertions.assertThat;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
@DisplayName("MatchingService")
class MatchingServiceTest {
    @Autowired MatchingService matchingService;
    final List<String> members = List.of("현준", "성우", "준혁");
    final int distance = 1000;

    @Test
    @DisplayName("matching을 생성한다")
    void runCreate() {

        StepVerifier.create(matchingService.create(members, distance))
                .assertNext(
                        matching -> {
                            assertThat(matching.getDistance()).isEqualTo(1000);
                            assertThat(matching.getStatus()).isEqualTo(MatchingStatus.WAIT);
                            assertThat(matching.getMembers().stream().map(MatchingMember::getId))
                                    .containsAll(members);
                        })
                .verifyComplete();
    }
}
