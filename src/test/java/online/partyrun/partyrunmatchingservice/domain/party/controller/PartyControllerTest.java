package online.partyrun.partyrunmatchingservice.domain.party.controller;

import online.partyrun.partyrunmatchingservice.config.docs.WebfluxDocsTest;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyEvent;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyIdResponse;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyRequest;
import online.partyrun.partyrunmatchingservice.domain.party.entity.PartyStatus;
import online.partyrun.partyrunmatchingservice.domain.party.exception.PartyNotFoundException;
import online.partyrun.partyrunmatchingservice.domain.party.service.PartyService;
import online.partyrun.partyrunmatchingservice.global.controller.HttpControllerAdvice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@ContextConfiguration(classes = {PartyController.class, HttpControllerAdvice.class})
@DisplayName("PartyController")
@WithMockUser
class PartyControllerTest extends WebfluxDocsTest {
    @MockBean
    PartyService partyService;

    private static final String ENTRY_CODE = "123456";

    @Test
    @DisplayName("post : party 생성")
    void postParties() {
        PartyRequest request = new PartyRequest(1000);
        given(partyService.create(any(Mono.class), any(PartyRequest.class)))
                .willReturn(Mono.just(new PartyIdResponse("123456")));

        client.post()
                .uri("/parties")
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .consumeWith(document("create-party"));
    }

    @Test
    @DisplayName("get : join party")
    void getPartyEventStream() {
        final Flux<PartyEvent> eventResult = Flux.just(
                new PartyEvent(ENTRY_CODE,1000, "member1", PartyStatus.WAITING, Set.of("member1"), null),
                new PartyEvent(ENTRY_CODE, 1000, "member1", PartyStatus.WAITING, Set.of("member1", "member2"), null),
                new PartyEvent(ENTRY_CODE, 1000, "member1", PartyStatus.COMPLETED, Set.of("member1", "member2"), "battle-id")
        );
        given(partyService.joinAndConnectSink(any(Mono.class), any(String.class)))
                .willReturn(eventResult);

        client.get()
                .uri("/parties/{entryCode}/join", ENTRY_CODE)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("join-party"));
    }

    @Test
    @DisplayName("post : start party")
    void partyStart() {
        given(partyService.start(any(Mono.class), any(String.class)))
                .willReturn(Mono.empty());

        client.post()
                .uri("/parties/{entryCode}/start",ENTRY_CODE)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .consumeWith(document("start-party"));
    }

    @Test
    @DisplayName("post : quit party")
    void quitParty() {
        given(partyService.start(any(Mono.class), any(String.class)))
                .willReturn(Mono.empty());

        client.post()
                .uri("/parties/{entryCode}/quit",ENTRY_CODE)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .consumeWith(document("quit-party"));
    }

    @Test
    @DisplayName("파티 join 시에 해당하는 entryCode가 없으면 예외")
    void throwExceptionIfPartyNotFound() {
        given(partyService.joinAndConnectSink(any(Mono.class), any(String.class)))
                .willReturn(Flux.error(new PartyNotFoundException("123456")));

        client.get()
                .uri("/parties/{entryCode}/join", ENTRY_CODE)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .consumeWith(document("throw-exception-when-party-not-found"));
    }

}