package online.partyrun.application.domain.match.controller;

import online.partyrun.application.domain.match.dto.MatchRequest;
import online.partyrun.application.domain.match.service.MatchService;
import online.partyrun.application.global.security.WebfluxAuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation;
import org.springframework.security.access.SecurityConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(
        controllers = MatchController.class,
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {WebfluxAuthFilter.class, SecurityConfig.class})
        })
@AutoConfigureRestDocs
@DisplayName("MatchController")
class MatchControllerTest {

    @MockBean
    MatchService matchService;

    private WebTestClient webTestClient;

    @Autowired private RestDocumentationContextProvider restDocumentationContextProvider;

    @BeforeEach
    public void setUp() {
        this.webTestClient =
                WebTestClient.bindToController(new MatchController(matchService))
                        .configureClient()
                        .filter(
                                WebTestClientRestDocumentation.documentationConfiguration(
                                        restDocumentationContextProvider))
                        .build();
    }

    @Test
    @DisplayName("post : match 수락 여부 전송")
    void postWaiting() {
        webTestClient
                .post()
                .uri("/match")
                .body(Mono.just(new MatchRequest(true)), MatchRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    @DisplayName("get : match event 요청")
    void getWaiting() {

        webTestClient.get().uri("/match/event").exchange().expectStatus().isOk();
    }
}
