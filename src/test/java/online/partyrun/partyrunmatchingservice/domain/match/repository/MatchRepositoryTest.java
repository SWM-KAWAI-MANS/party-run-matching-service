package online.partyrun.partyrunmatchingservice.domain.match.repository;

import static org.assertj.core.api.Assertions.assertThat;

import online.partyrun.partyrunmatchingservice.domain.match.domain.Match;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MatchMember;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MatchStatus;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MemberStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Stream;

@DataMongoTest
@DisplayName("MatchRepository")
class MatchRepositoryTest {

    @Autowired MatchRepository matchRepository;
    List<MatchMember> members = Stream.of("user1", "user2", "user3").map(MatchMember::new).toList();

    @AfterEach
    public void cleanup() {
        matchRepository.deleteAll().block();
    }

    @Test
    @DisplayName("생성을 수행한다")
    void runSave() {
        Match match = new Match(members, 1000);

        StepVerifier.create(matchRepository.save(match))
                .assertNext(
                        res -> {
                            assertThat(res).isEqualTo(match);
                            assertThat(res.getId()).isNotNull();
                            assertThat(res.getStatus()).isEqualTo(MatchStatus.WAIT);
                            assertThat(res.getMembers()).hasSize(3);
                            assertThat(res.getMembers().get(0).getId()).isNotNull();
                        })
                .verifyComplete();
    }

    @Test
    @DisplayName("전채 조회를 수행한다")
    void runFindAll() {
        matchRepository.save(new Match(members, 1000)).block();
        matchRepository.save(new Match(members, 1000)).block();
        matchRepository.save(new Match(members, 1000)).block();
        StepVerifier.create(matchRepository.findAll()).expectNextCount(3).verifyComplete();
    }

    @Test
    @DisplayName("러너 중에 무응답인 상태에 대해 탐색한다  ")
    void runFind() {
        final String targetUser = "user1";
        List<MatchMember> members =
                Stream.of(targetUser, "user2", "user3").map(MatchMember::new).toList();
        List<MatchMember> members2 =
                Stream.of("user4", "user5", "user6").map(MatchMember::new).toList();

        matchRepository.save(new Match(members, 1000)).block();
        final Match canceledMatch = new Match(members, 1000);
        canceledMatch.updateMemberStatus(targetUser, true);
        canceledMatch.updateMemberStatus("user2", true);
        canceledMatch.updateMemberStatus("user3", true);

        matchRepository.save(canceledMatch).block();
        matchRepository.save(new Match(members2, 2000)).block();

        StepVerifier.create(
                        matchRepository.findByMembersIdAndMembersStatus(
                                targetUser, MemberStatus.NO_RESPONSE))
                .assertNext(
                        res -> {
                            assertThat(res.getId()).isNotNull();
                            assertThat(res.getStatus()).isEqualTo(MatchStatus.WAIT);
                            assertThat(res.getMembers()).hasSize(3);
                            assertThat(res.getMembers().get(0).getId()).isNotNull();
                        })
                .verifyComplete();
    }
}