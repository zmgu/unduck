package com.ex.unduckauthservice.domain.oauth2.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2 인증 중 state 파라미터와 리다이렉트 서비스 정보를 임시 저장하는 Redis Repository
 */
@Repository
@RequiredArgsConstructor
public class OAuth2StateRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "oauth2:state:";
    private static final long TTL_MINUTES = 5; // OAuth2 인증은 보통 5분 이내 완료

    private String generateKey(String state) {
        return KEY_PREFIX + state;
    }

    public void saveState(String state, String redirectService) {
        String key = generateKey(state);
        redisTemplate.opsForValue().set(key, redirectService, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public Optional<String> getRedirectService(String state) {
        String key = generateKey(state);
        String redirectService = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(redirectService);
    }

    public void deleteState(String state) {
        String key = generateKey(state);
        redisTemplate.delete(key);
    }

    public boolean existsState(String state) {
        String key = generateKey(state);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}