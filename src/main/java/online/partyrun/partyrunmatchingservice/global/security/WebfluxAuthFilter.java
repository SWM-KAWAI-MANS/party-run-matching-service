package online.partyrun.partyrunmatchingservice.global.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.jwtmanager.JwtExtractor;
import online.partyrun.jwtmanager.dto.JwtPayload;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WebfluxAuthFilter implements WebFilter {
    JwtExtractor jwtExtractor;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final JwtPayload payload = getJwtPayload(exchange.getRequest());
        return chain.filter(exchange)
                .contextWrite(
                        ReactiveSecurityContextHolder.withAuthentication(new AuthUser(payload)));
    }

    private JwtPayload getJwtPayload(final ServerHttpRequest request) {
        final String token = request.getHeaders().getFirst("Authorization");
        return jwtExtractor.extract(token);
    }
}
