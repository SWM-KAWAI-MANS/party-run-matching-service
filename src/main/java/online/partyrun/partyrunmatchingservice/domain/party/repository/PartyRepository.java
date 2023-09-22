package online.partyrun.partyrunmatchingservice.domain.party.repository;

import online.partyrun.partyrunmatchingservice.domain.party.entity.EntryCode;
import online.partyrun.partyrunmatchingservice.domain.party.entity.Party;
import online.partyrun.partyrunmatchingservice.domain.party.entity.PartyStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface PartyRepository extends ReactiveMongoRepository<Party, String> {
    Mono<Party> findByEntryCodeAndStatus(EntryCode code, PartyStatus status);
}
