package online.partyrun.partyrunmatchingservice.config.docs;

import online.partyrun.partyrunmatchingservice.global.security.WebfluxAuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.access.SecurityConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {WebfluxAuthFilter.class, SecurityConfig.class})
        })
@ExtendWith(RestDocumentationExtension.class)
public abstract class WebfluxDocsTest {
    protected WebTestClient client;

    @Autowired private ApplicationContext context;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        client =
                WebTestClient.bindToApplicationContext(context)
                        .configureClient()
                        .filter(
                                documentationConfiguration(restDocumentation)
                                        .operationPreprocessors()
                                        .withRequestDefaults(prettyPrint())
                                        .withResponseDefaults(prettyPrint()))
                        .build()
                        .mutateWith(csrf());
    }
}
