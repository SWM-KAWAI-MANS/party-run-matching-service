package online.partyrun.application.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtExtractor {
    static int MIN_EXPIRE_SECONDS = 1;
    static String ID = "id";
    static String ROLE = "role";

    Key key;


    public JwtExtractor(@Value("${jwt.access-secret-key}")String key) {

        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }


    public JwtPayload extract(String accessToken) {
        final Claims claims = parseClaims(accessToken);
        final String id = claims.get(ID, String.class);
        final List<String> roles = claims.get(ROLE, ArrayList.class);
        final LocalDateTime expireAt =
                new Timestamp(claims.getExpiration().getTime()).toLocalDateTime();
        return new JwtPayload(id, roles, expireAt);
    }

    private Claims parseClaims(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }
}