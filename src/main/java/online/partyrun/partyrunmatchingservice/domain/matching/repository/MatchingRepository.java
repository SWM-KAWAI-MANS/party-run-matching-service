package online.partyrun.partyrunmatchingservice.domain.matching.repository;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface MatchingRepository extends ReactiveMongoRepository<Matching, String> {
    Mono<Matching> findByMembersIdAndMembersStatus(
            String memberId, MatchingMemberStatus memberStatus);
}

