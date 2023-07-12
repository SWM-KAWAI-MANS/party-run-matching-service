package online.partyrun.application.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import online.partyrun.application.global.security.jwt.JwtPayload;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

@DisplayName("AuthUser")
class AuthUserTest {

    @Test
    @DisplayName("Payload가 주어지면 AuthUser를 생성한다.")
    void mapRole() {
        final List<String> roles = List.of("USER", "ADMIN");
        JwtPayload payload = new JwtPayload("현준", roles, LocalDateTime.now());
        AuthUser authUser = new AuthUser(payload);

        assertAll(
                () -> assertThat(authUser.getName()).isEqualTo(payload.id()),
                () -> assertThat(authUser.getAuthorities().size()).isEqualTo(roles.size()));
    }
}
