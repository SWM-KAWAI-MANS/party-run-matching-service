package online.partyrun.application.global.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.jwtmanager.JwtExtractor;
import online.partyrun.jwtmanager.dto.JwtPayload;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Webflux 환경에서 인가 필터를 구성합니다. JWT 토큰 기반으로 인가를 진행합니다.
 *
 * @author parkhyeonjun
 * @see JwtExtractor
 * @see online.partyrun.jwtmanager.manager.JwtManager
 * @since 2023.06.29
 */
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WebfluxAuthFilter implements WebFilter {
    JwtExtractor jwtExtractor;

    /**
     * filter가 수행할 떄 request header에서 token을 추출하여 해당 정보를 AuthUser로 생성합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        final JwtPayload payload = getJwtPayload(exchange);
        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(c -> c.setAuthentication(new AuthUser(payload)))
                .then(chain.filter(exchange));
    }

    /**
     * header에서 token을 추출합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private JwtPayload getJwtPayload(final ServerWebExchange exchange) {
        final String token =
                exchange.getRequest().getHeaders().get("Authorization").stream()
                        .findFirst()
                        .orElseThrow(AuthorizationException::new);
        final JwtPayload payload = jwtExtractor.extract(token);
        return payload;
    }
}
