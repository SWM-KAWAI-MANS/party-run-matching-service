package online.partyrun.partyrunmatchingservice.domain.matching.controller;

import online.partyrun.partyrunmatchingservice.config.docs.WebfluxDocsTest;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchingResponse;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.service.MatchingService;
import online.partyrun.partyrunmatchingservice.global.controller.HttpControllerAdvice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@ContextConfiguration(classes = {MatchingController.class, HttpControllerAdvice.class})
@DisplayName("MatchingController")
@WithMockUser
class MatchingControllerTest extends WebfluxDocsTest {
    @MockBean MatchingService matchingService;
    final Matching matching =
            new Matching(
                    List.of(new MatchingMember("현준"), new MatchingMember("준혁")),
                    1000,
                    LocalDateTime.now());

    @Test
    @DisplayName("post : match 수락 여부 전송")
    void postMatching() {
        given(matchingService.setMemberStatus(any(Mono.class), any(MatchingRequest.class)))
                .willReturn(Mono.empty());

        client.post()
                .uri("/matching/members/join")
                .bodyValue(new MatchingRequest(true))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .consumeWith(document("create-matching"));
    }

    @Test
    @DisplayName("get : waiting event 요청")
    void getMatching() {
        given(matchingService.getEventSteam(any(Mono.class)))
                .willReturn(Flux.just(new MatchEvent(matching), new MatchEvent(matching)));
        client.get()
                .uri("/matching/event")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("get-matching-event"));
    }

    @Test
    @DisplayName("get : matching 탐색")
    void getMatchingById() {
        given(matchingService.getById(any(String.class)))
                .willReturn(Mono.just(new MatchingResponse(matching)));

        client.get()
                .uri("/matching/{}", matching.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("get-matching-by-id"));
    }
}
