package online.partyrun.partyrunmatchingservice.config.security;

import online.partyrun.partyrunmatchingservice.global.security.WebfluxAuthFilter;
import online.partyrun.partyrunmatchingservice.global.security.jwt.JwtExtractor;

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
