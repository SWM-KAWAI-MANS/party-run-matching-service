package online.partyrun.partyrunmatchingservice.domain.matching.repository;

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

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("MatchingRepository")
class MatchingRepositoryTest {
    @Autowired
    MatchingRepository matchRepository;
    List<MatchingMember> members = Stream.of("user1", "user2", "user3").map(MatchingMember::new).toList();

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
    @DisplayName("러너 중에 무응답인 상태에 대해 탐색한다")
    void runFind() {
        final String targetUser = "user1";
        List<MatchingMember> members =
                Stream.of(targetUser, "user2", "user3").map(MatchingMember::new).toList();
        List<MatchingMember> members2 =
                Stream.of("user4", "user5", "user6").map(MatchingMember::new).toList();

        matchRepository.save(new Matching(members, 1000)).block();
        final Matching canceledMatch = new Matching(members, 1000);
        canceledMatch.updateMemberStatus(targetUser, true);
        canceledMatch.updateMemberStatus("user2", true);
        canceledMatch.updateMemberStatus("user3", true);

        matchRepository.save(canceledMatch).block();
        matchRepository.save(new Matching(members2, 2000)).block();

        StepVerifier.create(
                        matchRepository.findByMembersIdAndMembersStatus(
                                targetUser, MatchingMemberStatus.NO_RESPONSE))
                .assertNext(
                        res -> {
                            assertThat(res.getId()).isNotNull();
                            assertThat(res.getStatus()).isEqualTo(MatchingStatus.WAIT);
                            assertThat(res.getMembers()).hasSize(3);
                            assertThat(res.getMembers().get(0).getId()).isNotNull();
                        })
                .verifyComplete();
    }
}