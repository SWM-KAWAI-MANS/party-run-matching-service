package online.partyrun.application.config.security;

import online.partyrun.application.global.security.WebfluxAuthFilter;
import online.partyrun.application.global.security.jwt.JwtExtractor;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SecurityTestConfig {
    @Bean
    public WebfluxAuthFilter webfluxAuthFilter() {
        return new WebfluxAuthFilter(jwtExtractor());
    }

    @Bean
    public JwtExtractor jwtExtractor() {
        return new JwtExtractor(
                "asfasdfaasfasdfasdfadfasdfasdfasdfaasfasdfasdfadfasdfasdfasdfaasfasdfasdfadfasdfasdfasdfaasfasdfasdfadfasdfasdfasdfaasfasdfasdfadfasd");
    }
}
