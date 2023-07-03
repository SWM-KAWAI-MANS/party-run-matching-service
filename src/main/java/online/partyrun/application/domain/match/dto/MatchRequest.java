package online.partyrun.application.domain.match.dto;

/**
 * match 참여에 관한 요청을 보낼 때 필요한 요구사항을 정리해놓은 DTO record 입니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
public record MatchRequest(boolean isJoin) {}
