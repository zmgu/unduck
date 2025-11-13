package com.ex.unduckauthservice.domain.jwt.service;

import com.ex.unduckauthservice.domain.jwt.entity.RefreshTokenEntity;
import com.ex.unduckauthservice.domain.jwt.repository.RefreshTokenRedisRepository;
import com.ex.unduckauthservice.util.CookieUtil;
import com.ex.unduckauthservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    /**
     * ❌ 삭제됨 - cookie2Header
     * 이제 로그인 시 즉시 두 토큰을 HttpOnly 쿠키로 발급
     */

    /**
     * HttpOnly 쿠키의 Refresh Token으로 Access Token 재발급
     * - 쿠키에서 Refresh Token 추출
     * - 검증 후 새 Access Token + Refresh Token 생성
     * - 새 토큰들을 HttpOnly 쿠키로 발급
     */
    public void refreshTokensViaCookie(HttpServletRequest request, HttpServletResponse response) {

        // 1. 쿠키에서 Refresh Token 추출
        Optional<String> refreshTokenOpt = CookieUtil.getCookieValue(request, "refreshToken");

        if (refreshTokenOpt.isEmpty()) {
            // ✅ 비로그인 상태 - 401 반환 (예외 던지지 않음)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String refreshToken = refreshTokenOpt.get();

        // 2. Refresh Token 검증
        if (!JwtUtil.isValid(refreshToken, false)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 3. Redis에 존재하는지 확인 (화이트리스트)
        if (!existsRefreshToken(refreshToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 4. 토큰에서 정보 추출
        String username = JwtUtil.getUsername(refreshToken);
        String role = JwtUtil.getRole(refreshToken);

        // 5. 새 토큰 생성
        String newAccessToken = JwtUtil.createJWT(username, role, true);
        String newRefreshToken = JwtUtil.createJWT(username, role, false);

        // 6. 기존 Refresh Token 삭제 후 신규 추가
        removeRefreshToken(refreshToken);
        saveRefreshToken(newRefreshToken, username);

        // 7. 새 토큰들을 HttpOnly 쿠키로 발급
        CookieUtil.addSecureCookie(response, "accessToken", newAccessToken, 3600); // 1시간
        CookieUtil.addSecureCookie(response, "refreshToken", newRefreshToken, 604800); // 7일

        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Refresh Token을 Redis에 저장
     */
    public void saveRefreshToken(String refreshToken, String username) {
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .refreshToken(refreshToken)
                .username(username)
                .build();

        refreshTokenRedisRepository.refreshTokenSave(refreshTokenEntity);
    }

    /**
     * Refresh Token 존재 여부 확인
     */
    public Boolean existsRefreshToken(String refreshToken) {
        return refreshTokenRedisRepository.refreshTokenExists(refreshToken);
    }

    /**
     * Refresh Token 삭제
     */
    public void removeRefreshToken(String refreshToken) {
        refreshTokenRedisRepository.refreshTokenDelete(refreshToken);
    }
}