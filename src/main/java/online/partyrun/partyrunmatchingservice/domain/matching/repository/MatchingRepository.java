package online.partyrun.partyrunmatchingservice.domain.matching.repository;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MatchingRepository extends ReactiveMongoRepository<Matching, String> {
    Flux<Matching> findAllByMembersIdInAndMembersStatus(
            List<String> memberIds, MatchingMemberStatus memberStatus);

    Mono<Matching> findByMembersIdAndMembersStatus(String id, MatchingMemberStatus status);

    Mono<Matching> findFirstByMembersIdOrderByStartAtDesc(String id);

    @Query("{'id': ?0, 'members.id': ?1}")
    @Update("{$set : {'members.$.status': ?2}}")
    Mono<Void> updateMatchingMemberStatus(
            String matchingId, String memberId, MatchingMemberStatus status);

    Flux<Matching> findAllByMembersStatus(MatchingMemberStatus status);
}
