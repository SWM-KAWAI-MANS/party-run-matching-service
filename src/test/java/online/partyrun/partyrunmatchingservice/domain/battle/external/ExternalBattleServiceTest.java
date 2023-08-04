package online.partyrun.partyrunmatchingservice.domain.battle.external;

import online.partyrun.partyrunmatchingservice.domain.battle.BattleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExternalBattleServiceTest {

    @Autowired
    BattleService battleService;


    @Test
    @DisplayName("실제 생성 요청을 보낸다")
    void runCreate() {
        List<String> ids = List.of("64c4652b944dee25e48fc599", "64c4c1f1944dee25e48fc59a");

        final String result = battleService.create(ids, 1000).block();
        System.out.println(result);
        assertThat(result).isNotNull();
    }

}