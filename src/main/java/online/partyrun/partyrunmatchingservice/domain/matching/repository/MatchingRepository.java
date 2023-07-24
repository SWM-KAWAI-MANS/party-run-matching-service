package online.partyrun.partyrunmatchingservice.domain.matching.repository;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;

import java.util.List;

public interface MatchingRepository extends ReactiveMongoRepository<Matching, String> {
    Flux<Matching> findAllByMembersIdInAndMembersStatus(
            List<String> memberIds, MatchingMemberStatus memberStatus);
}
