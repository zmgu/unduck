package com.ex.unduckauthservice.domain.jwt.repository;

import com.ex.unduckauthservice.domain.jwt.entity.RefreshTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private String generateKey(String refreshToken) {
        return "refreshToken:" + refreshToken;
    }

    public void refreshTokenSave(RefreshTokenEntity refreshTokenEntity) {
        String key = generateKey(refreshTokenEntity.getRefreshToken());
        redisTemplate.opsForValue().set(key, refreshTokenEntity.getUsername());
    }

    public boolean refreshTokenExists(String refreshToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(generateKey(refreshToken)));
    }

    public void refreshTokenDelete(String refreshToken) {
        redisTemplate.delete(generateKey(refreshToken));
    }
}
