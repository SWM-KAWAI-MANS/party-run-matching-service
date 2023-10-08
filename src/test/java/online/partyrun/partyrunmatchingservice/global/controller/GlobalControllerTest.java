package online.partyrun.partyrunmatchingservice.global.controller;

import online.partyrun.partyrunmatchingservice.config.docs.WebfluxDocsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {GlobalController.class, HttpControllerAdvice.class})
@WithMockUser
@DisplayName("GlobalController")
class GlobalControllerTest extends WebfluxDocsTest {
    @Test
    @DisplayName("health check")
    void healthCheck() {
        client.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk();
    }
}