package online.partyrun.application.domain.waiting.controller;

import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.application.domain.waiting.service.WaitingService;
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
        controllers = WaitingController.class,
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {WebfluxAuthFilter.class, SecurityConfig.class})
        })
@AutoConfigureRestDocs
class WaitingControllerTest {
    @MockBean WaitingService waitingService;

    private WebTestClient webTestClient;

    @Autowired private RestDocumentationContextProvider restDocumentationContextProvider;

    @BeforeEach
    public void setUp() {
        this.webTestClient =
                WebTestClient.bindToController(new WaitingController(waitingService))
                        .configureClient()
                        .filter(
                                WebTestClientRestDocumentation.documentationConfiguration(
                                        restDocumentationContextProvider))
                        .build();
    }

    @Test
    @DisplayName("post : waiting 생성 요청")
    void postWaiting() {
        webTestClient
                .post()
                .uri("/waiting")
                .body(
                        Mono.just(new CreateWaitingRequest(RunningDistance.M1000)),
                        CreateWaitingRequest.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    @DisplayName("get : waiting event 요청")
    void getWaiting() {

        webTestClient.get().uri("/waiting/event").exchange().expectStatus().isOk();
    }
}
