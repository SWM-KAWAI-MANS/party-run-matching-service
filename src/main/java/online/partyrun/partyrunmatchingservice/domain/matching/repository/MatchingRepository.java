package online.partyrun.partyrunmatchingservice.domain.matching.repository;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MatchingRepository extends ReactiveMongoRepository<Matching, String> {}
