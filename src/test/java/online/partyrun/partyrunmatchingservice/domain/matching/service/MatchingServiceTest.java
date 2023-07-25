package online.partyrun.partyrunmatchingservice.domain.matching.service;

import static org.assertj.core.api.Assertions.assertThat;

import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;
import online.partyrun.partyrunmatchingservice.global.sse.ServerSentEventHandler;

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

    @Autowired ServerSentEventHandler<String, MatchEvent> sseHandler;
    @Autowired MatchingRepository matchingRepository;

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

    @Test
    @DisplayName("match 생성 시 sink connect를 생성한다")
    void runCreateSink() {
        matchingService.create(members, distance).block();
        assertThat(sseHandler.getConnectors()).containsAll(members);
    }

    @Test
    @DisplayName("match 생성 시 기존 sink가 남아있으면 완료한 후에 재연결한다.")
    void runDeleteSinkBeforeCreate() {
        final Matching matching = matchingService.create(members, distance).block();
        matching.updateMemberStatus(members.get(0), MatchingMemberStatus.CANCELED);
        matchingRepository.save(matching).block();

        matchingService.create(members, distance).block();

        assertThat(sseHandler.getConnectors().stream().filter(m -> m.equals(members.get(0))))
                .hasSize(1);
    }
}
