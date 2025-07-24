package com.aldhafara.lightPollutionService.ratelimit;

import com.aldhafara.lightPollutionService.exception.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {

    private final Environment env;

    private final Map<String, RateLimiter> requestMap = new ConcurrentHashMap<>();

    public RateLimitAspect(Environment env) {
        this.env = env;
    }

    @Around("@annotation(rateLimited)")
    public Object rateLimit(ProceedingJoinPoint pjp, RateLimited rateLimited) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String key = request.getRemoteAddr() + ":" +
                pjp.getSignature().toShortString();

        int requests = rateLimited.requests() == -1
                ? Integer.parseInt(env.getProperty("ratelimit.requests", "50"))
                : rateLimited.requests();

        int duration = rateLimited.durationSeconds() == -1
                ? Integer.parseInt(env.getProperty("ratelimit.durationSeconds", "60"))
                : rateLimited.durationSeconds();

        RateLimiter limiter = requestMap.computeIfAbsent(
                key, k -> {
                    return new RateLimiter(requests, duration);
                }
        );

        boolean granted = limiter.grantAccess();

        if (granted) {
            return pjp.proceed();
        } else {
            throw new RateLimitException("Too many requests");
        }
    }

    public void resetLimiters() {
        requestMap.values().forEach(RateLimiter::reset);
        requestMap.clear();
    }

    private static class RateLimiter {
        private final int durationSeconds;
        private final int maxRequests;
        private int requests;
        private long windowStart;

        RateLimiter(int maxRequests, int durationSeconds) {
            this.maxRequests = maxRequests;
            this.durationSeconds = durationSeconds;
            this.requests = 0;
            this.windowStart = Instant.now().toEpochMilli();
        }

        synchronized boolean grantAccess() {
            long now = Instant.now().toEpochMilli();
            if (now - windowStart >= durationSeconds * 1000L) {
                requests = 0;
                windowStart = now;
            }
            if (requests < maxRequests) {
                requests++;
                return true;
            }
            return false;
        }

        public void reset() {
            this.requests = 0;
            this.windowStart = Instant.now().toEpochMilli();
        }
    }
}
