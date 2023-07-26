package online.partyrun.partyrunmatchingservice.domain.matching.repository;

import static org.assertj.core.api.Assertions.assertThat;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Stream;

@DataMongoTest
@DisplayName("MatchingRepository")
class MatchingRepositoryTest {
    @Autowired MatchingRepository matchRepository;
    List<MatchingMember> members =
            Stream.of("user1", "user2", "user3").map(MatchingMember::new).toList();

    @AfterEach
    public void cleanup() {
        matchRepository.deleteAll().block();
    }

    @Test
    @DisplayName("생성을 수행한다")
    void runSave() {
        Matching match = new Matching(members, 1000);

        StepVerifier.create(matchRepository.save(match))
                .assertNext(
                        res -> {
                            assertThat(res).isEqualTo(match);
                            assertThat(res.getId()).isNotNull();
                            assertThat(res.getStatus()).isEqualTo(MatchingStatus.WAIT);
                            assertThat(res.getMembers()).hasSize(3);
                            assertThat(res.getMembers().get(0).getId()).isNotNull();
                        })
                .verifyComplete();
    }

    @Test
    @DisplayName("전채 조회를 수행한다")
    void runFindAll() {
        matchRepository.save(new Matching(members, 1000)).block();
        matchRepository.save(new Matching(members, 1000)).block();
        matchRepository.save(new Matching(members, 1000)).block();
        StepVerifier.create(matchRepository.findAll()).expectNextCount(3).verifyComplete();
    }

    @Test
    @DisplayName("matching member 상태를 변경한다")
    void updateMatchingMemberStatus() {
        final Matching matching = matchRepository.save(new Matching(members, 1000)).block();

        matchRepository
                .updateMatchingMemberStatus(
                        matching.getId(), members.get(0).getId(), MatchingMemberStatus.READY)
                .block();

        final List<MatchingMember> getMembers =
                matchRepository.findById(matching.getId()).block().getMembers();

        assertThat(getMembers.get(0).getStatus()).isEqualTo(MatchingMemberStatus.READY);
        assertThat(getMembers.get(1).getStatus()).isEqualTo(MatchingMemberStatus.NO_RESPONSE);
        assertThat(getMembers.get(2).getStatus()).isEqualTo(MatchingMemberStatus.NO_RESPONSE);
    }
}
