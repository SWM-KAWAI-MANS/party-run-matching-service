package online.partyrun.partyrunmatchingservice.domain.waiting.controller;

import online.partyrun.partyrunmatchingservice.config.docs.WebfluxDocsTest;
import online.partyrun.partyrunmatchingservice.domain.waiting.domain.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.domain.WaitingEvent;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.partyrunmatchingservice.domain.waiting.service.WaitingService;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;
import org.junit.jupiter.api.*;
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
                .isCreated()
                .expectBody()
                .consumeWith(document("create-waiting"));
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class If_distance_was_null {
        @Test
        @DisplayName("예외을 반환한다")
        void throwException() {
            final CreateWaitingRequest request = new CreateWaitingRequest(null);
            client.post()
                    .uri("/waiting")
                    .bodyValue(request)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .consumeWith(document("create-waiting-throw-null"));
        }
    }

    @Test
    @DisplayName("get : waiting event 요청")
    void getWaiting() {
        given(waitingService.subscribe(any()))
                .willReturn(
                        Flux.just(
                                new WaitingEventResponse(WaitingEvent.CONNECT),
                                new WaitingEventResponse(WaitingEvent.MATCHED)));
        client.get()
                .uri("/waiting/event")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("get-waiting-event"));
    }
}