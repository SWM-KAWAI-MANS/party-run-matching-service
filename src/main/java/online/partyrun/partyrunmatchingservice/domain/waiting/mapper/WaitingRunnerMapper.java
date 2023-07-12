package online.partyrun.partyrunmatchingservice.domain.waiting.mapper;

import online.partyrun.partyrunmatchingservice.domain.waiting.domain.WaitingUser;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import org.springframework.stereotype.Component;

/**
 * WaitingRunner 도메인과 dto를 변환하는 역할을 하는 mapper 클래스 입니다.
 *
 * @author parkhyeonjun
 * @see WaitingUser
 * @see CreateWaitingRequest
 * @since 2023.06.29
 */
@Component
public class WaitingRunnerMapper {
    public WaitingUser toEntity(String id, CreateWaitingRequest request) {
        return new WaitingUser(id, request.distance());
    }
}
