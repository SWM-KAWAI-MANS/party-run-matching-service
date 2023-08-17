package online.partyrun.partyrunmatchingservice.global.advisor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class LogAdvisor {

    // partyrunservice 속한 모든 패키지의 모든 클래스의 모든 메서드
    @Pointcut("within(online.partyrun.partyrunmatchingservice..*)")
    public void allComponents() {}

    @Before("allComponents()")
    public void generateTraceLong(JoinPoint joinPoint) {
        log.debug("{}.{}({})",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                joinPoint.getArgs());
    }
}
