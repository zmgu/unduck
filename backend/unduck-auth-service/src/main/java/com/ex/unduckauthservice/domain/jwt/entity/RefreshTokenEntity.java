package com.ex.unduckauthservice.domain.jwt.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "refreshToken", timeToLive = 60*60)
@Builder
@AllArgsConstructor
public class RefreshTokenEntity {

    @Id
    private String refreshToken;
    private String username;

}
