package com.agenticurlshortner.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OrchestratorMetricsAspect {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorMetricsAspect.class);

    @Around("execution(* com.agenticurlshortner.orchestration.WorkflowOrchestrator.*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        boolean success = true;

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            success = false;
            throw ex;
        } finally {
            long latency = System.currentTimeMillis() - startTime;
            log.info("[AUDIT METRIC] Target: {} | Latency: {}ms | Success: {}",
                    joinPoint.getSignature().getName(), latency, success);
        }
    }
}