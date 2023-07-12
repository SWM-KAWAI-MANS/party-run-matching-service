package online.partyrun.application.domain.waiting.controller;

import online.partyrun.application.config.docs.WebfluxDocsTest;
import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingEvent;
import online.partyrun.application.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.application.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.application.domain.waiting.service.WaitingService;
import online.partyrun.application.global.dto.MessageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@ContextConfiguration(classes = WaitingController.class)
@WithMockUser
@DisplayName("WaitingController")
class WaitingControllerTest extends WebfluxDocsTest {

    @MockBean WaitingService waitingService;
    @Test
    @DisplayName("post : waiting 생성 요청")
    void postWaiting() {
        final CreateWaitingRequest request = new CreateWaitingRequest(RunningDistance.M1000);
        Mono<MessageResponse> response = Mono.just(new MessageResponse("testUser님 대기열 등록"));
        given(waitingService.register(any(), any())).willReturn(response);
        client.post()
                .uri("/waiting")
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated().expectBody()
                .consumeWith(document("create-waiting"));
    }

    @Test
    @DisplayName("get : waiting event 요청")
    void getWaiting() {
        given(waitingService.subscribe(any()))
                .willReturn(Flux.just(new WaitingEventResponse(WaitingEvent.CONNECT), new WaitingEventResponse(WaitingEvent.MATCHED)));
        client.get().uri("/waiting/event")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk().expectBody().consumeWith(document("get-waiting-event"));
    }
}
