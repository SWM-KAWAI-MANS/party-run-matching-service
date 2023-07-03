package online.partyrun.application.domain.waiting.mapper;

import online.partyrun.application.domain.waiting.domain.WaitingRunner;
import online.partyrun.application.domain.waiting.dto.CreateWaitingRequest;
import org.springframework.stereotype.Component;

/**
 * WaitingRunner 도메인과 dto를 변환하는 역할을 하는 mapper 클래스 입니다.
 *
 * @author parkhyeonjun
 * @see WaitingRunner
 * @see CreateWaitingRequest
 * @since 2023.06.29
 */
@Component
public class WaitingRunnerMapper {
    public WaitingRunner toEntity(String id, CreateWaitingRequest request) {
        return new WaitingRunner(id, request.distance());
    }
}
