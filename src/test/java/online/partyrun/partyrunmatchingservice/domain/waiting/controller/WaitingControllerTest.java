package online.partyrun.partyrunmatchingservice.domain.waiting.controller;

import online.partyrun.partyrunmatchingservice.config.docs.WebfluxDocsTest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.InvalidDistanceException;
import online.partyrun.partyrunmatchingservice.domain.waiting.service.WaitingEventService;
import online.partyrun.partyrunmatchingservice.domain.waiting.service.CreateWaitingService;
import online.partyrun.partyrunmatchingservice.global.controller.HttpControllerAdvice;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;
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

@ContextConfiguration(classes = {WaitingController.class, HttpControllerAdvice.class})
@WithMockUser
@DisplayName("WaitingController")
class WaitingControllerTest extends WebfluxDocsTest {
    @MockBean
    CreateWaitingService createWaitingService;
    @MockBean WaitingEventService waitingEventService;

    @Test
    @DisplayName("post : waiting 생성 요청")
    void postWaiting() {
        final CreateWaitingRequest request = new CreateWaitingRequest(1000);
        final Mono<MessageResponse> response = Mono.just(new MessageResponse("testUser님 대기열 등록"));
        given(createWaitingService.create(any(Mono.class), any(CreateWaitingRequest.class)))
                .willReturn(response);

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

    @Test
    @DisplayName("get : WaitingEventStream 요청")
    void getWaitingEventStream() {

        given(waitingEventService.getEventStream(any(Mono.class)))
                .willReturn(Flux.just(WaitingStatus.CONNECTED, WaitingStatus.MATCHED));

        client.get()
                .uri("/waiting/event")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("get-waiting-event"));
    }

    @Test
    @DisplayName("distance값을_적절하게_요청하지_않으면_예외을 반환한다")
    void throwException() {
        final int distance = 100;
        given(createWaitingService.create(any(Mono.class), any(CreateWaitingRequest.class)))
                .willThrow(new InvalidDistanceException(distance));

        final CreateWaitingRequest request = new CreateWaitingRequest(distance);
        client.post()
                .uri("/waiting")
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .consumeWith(document("create-waiting-throw-distance-was-bad-value"));
    }

    @Test
    @DisplayName("sink 및 대기열 전체 삭제를 진행한다.")
    void removeAllSink() {
        client.delete()
                .uri("/waiting")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .consumeWith(document("shutdown"));
    }

    @Test
    @DisplayName("event cancel을 수행한다")
    void cancelEvent() {
        given(waitingEventService.cancel(any(Mono.class)))
                .willReturn(Mono.just(new MessageResponse("cancelled")));

        client.post()
                .uri("/waiting/event/cancel")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("cancel-waiting-event"));
    }
}
