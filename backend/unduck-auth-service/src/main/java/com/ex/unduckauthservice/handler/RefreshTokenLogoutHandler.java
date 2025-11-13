package com.ex.unduckauthservice.handler;

import com.ex.unduckauthservice.domain.jwt.service.JwtService;
import com.ex.unduckauthservice.util.CookieUtil;
import com.ex.unduckauthservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RefreshTokenLogoutHandler implements LogoutHandler {

    private final JwtService jwtService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        try {
            // 1. 쿠키에서 Refresh Token 추출
            Optional<String> refreshTokenOpt = CookieUtil.getCookieValue(request, "refreshToken");

            if (refreshTokenOpt.isPresent()) {
                String refreshToken = refreshTokenOpt.get();

                // 2. Refresh Token 유효성 검증
                if (JwtUtil.isValid(refreshToken, false)) {
                    // 3. Redis에서 Refresh Token 삭제
                    jwtService.removeRefreshToken(refreshToken);
                    log.info("✅ Refresh Token 삭제 완료");
                } else {
                    log.warn("⚠️ 유효하지 않은 Refresh Token");
                }
            } else {
                log.warn("⚠️ Refresh Token 쿠키가 없습니다.");
            }

            // 4. 쿠키 삭제 (Access Token + Refresh Token)
            CookieUtil.deleteCookie(response, "accessToken");
            CookieUtil.deleteCookie(response, "refreshToken");
            log.info("✅ 쿠키 삭제 완료");

        } catch (Exception e) {
            log.error("❌ 로그아웃 처리 중 오류 발생", e);
        }
    }
}