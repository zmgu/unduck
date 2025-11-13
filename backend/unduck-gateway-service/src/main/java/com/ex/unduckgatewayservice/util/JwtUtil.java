package com.ex.unduckgatewayservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class JwtUtil {

    private static final SecretKey secretKey;

    static {
        String secretKeyString = "himynameiskimjihunmyyoutubechann";
        secretKey = new SecretKeySpec(
                secretKeyString.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    // JWT 토큰에서 username 추출
    public static String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("sub", String.class);
    }

    //JWT 토큰에서 role 추출
    public static String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    /**
     * Access Token 유효성 검증
     * - 서명 검증
     * - 만료 시간 검증
     * - 타입이 "access"인지 검증
     */
    public static Boolean isValidAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get("type", String.class);
            return "access".equals(type);

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
