package online.partyrun.application.global.security.jwt;

import java.time.LocalDateTime;
import java.util.List;

public record JwtPayload(String id, List<String> roles, LocalDateTime expireAt) {}
