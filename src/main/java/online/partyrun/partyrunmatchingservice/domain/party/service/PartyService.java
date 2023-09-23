package online.partyrun.partyrunmatchingservice.domain.party.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.battle.service.BattleService;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyEvent;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyIdResponse;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyRequest;
import online.partyrun.partyrunmatchingservice.domain.party.entity.EntryCode;
import online.partyrun.partyrunmatchingservice.domain.party.entity.Party;
import online.partyrun.partyrunmatchingservice.domain.party.entity.PartyStatus;
import online.partyrun.partyrunmatchingservice.domain.party.repository.PartyRepository;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PartyService {
    PartyRepository partyRepository;
    PartySinkHandler partySinkHandler;
    BattleService battleService;

    public Mono<PartyIdResponse> create(Mono<String> member, PartyRequest request) {
        return member.map(memberId ->
                        new Party(memberId, RunningDistance.getBy(request.distance())))
                .flatMap(partyRepository::save)
                .map(PartyIdResponse::new);
    }

    public Flux<PartyEvent> joinAndConnectSink(Mono<String> member, String entryCode) {
        return member
                .doOnNext(partySinkHandler::create)
                .flatMap(memberId -> joinParty(memberId, entryCode))
                .then(member)
                .flatMapMany(partySinkHandler::connect);
    }

    private Mono<Void> joinParty(String memberId, String entryCode) {
        return getWaitingParty(entryCode)
                .flatMap(party -> {
                            party.join(memberId);
                            return partyRepository.save(party);
                        }
                )
                .doOnNext(this::multicast)
                .then();
    }

    private void multicast(Party party) {
        party.getParticipants().forEach(
                member -> {
                    partySinkHandler.sendEvent(member, new PartyEvent(party));
                    if (party.isRecruitClosed()) {
                        partySinkHandler.complete(member);
                    }
                });
    }

    private Mono<Party> getWaitingParty(String code) {
        return partyRepository.findByEntryCodeAndStatus(new EntryCode(code), PartyStatus.WAITING);
    }

    public Mono<Void> start(Mono<String> member, String code) {
        // TODO 방장 여부 확인
        return getWaitingParty(code).flatMap(party ->
                        battleService.create(party.getParticipants(), party.getDistance().getMeter())
                ).flatMap(battleId ->
                        getWaitingParty(code).doOnNext(party -> party.start(battleId))
                                .flatMap(partyRepository::save))
                .doOnNext(this::multicast).then();
    }

    public Mono<Void> quit(Mono<String> member, String code) {
        return getWaitingParty(code)
                .flatMap(party ->
                        member.flatMap(memberId -> {
                            party.quit(memberId);
                            partySinkHandler.complete(memberId);
                            return partyRepository.save(party);
                        })
                )
                .doOnNext(this::multicast)
                .then();
    }
}
