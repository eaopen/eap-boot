package org.openea.eap.framework.ratelimiter.core.keyresolver;

import org.aspectj.lang.JoinPoint;
import org.openea.eap.framework.ratelimiter.core.annotation.RateLimiter;

/**
 * 限流 Key 解析器接口
 *
 */
public interface RateLimiterKeyResolver {

    /**
     * 解析一个 Key
     *
     * @param rateLimiter 限流注解
     * @param joinPoint  AOP 切面
     * @return Key
     */
    String resolver(JoinPoint joinPoint, RateLimiter rateLimiter);

}
