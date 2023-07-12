package online.partyrun.partyrunmatchingservice.global.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Webflux 환경에서 Security를 구성합니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
@Configuration
@EnableWebFluxSecurity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WebfluxSecurityConfig {
    WebfluxAuthFilter webfluxAuthFilter;

    /**
     * Webflux 환경에서 {@link SecurityWebFilterChain}을 구성합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http.authorizeExchange(getAuthorizeExchangeSpecCustomizer())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterBefore(webfluxAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .build();
    }

    /**
     * CORS 정책을 설정합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");
        config.addAllowedOrigin("https://**");
        config.addAllowedHeader("*");
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 각 경로에 관하여 필요 role을 정의합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private Customizer<ServerHttpSecurity.AuthorizeExchangeSpec>
            getAuthorizeExchangeSpecCustomizer() {
        return r -> r.pathMatchers("/**").permitAll();
    }
}
