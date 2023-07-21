package online.partyrun.partyrunmatchingservice.domain.waiting.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

import online.partyrun.partyrunmatchingservice.config.docs.WebfluxDocsTest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.service.WaitingService;
import online.partyrun.partyrunmatchingservice.global.dto.MessageResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

import reactor.core.publisher.Mono;

@ContextConfiguration(classes = WaitingController.class)
@WithMockUser
@DisplayName("WaitingController")
class WaitingControllerTest extends WebfluxDocsTest {
    @MockBean WaitingService waitingService;

    @Test
    @DisplayName("post : waiting 생성 요청")
    void postWaiting() {
        final CreateWaitingRequest request = new CreateWaitingRequest(1000);
        final Mono<MessageResponse> response = Mono.just(new MessageResponse("testUser님 대기열 등록"));
        given(waitingService.create(any(), any())).willReturn(response);

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
}
