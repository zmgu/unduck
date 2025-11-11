package com.ex.unduckauthservice.handler;

import com.ex.unduckauthservice.domain.jwt.entity.RefreshTokenEntity;
import com.ex.unduckauthservice.domain.jwt.service.JwtService;
import com.ex.unduckauthservice.domain.jwt.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Qualifier("SocialSuccessHandler")
@RequiredArgsConstructor
public class SocialSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // 1️⃣ 인증된 사용자 정보
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // 2️⃣ Refresh Token 발급
        String refreshToken = JwtUtil.createJWT(username, "ROLE_" + role, false);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .refreshToken(refreshToken)
                .username(username)
                .build();

        jwtService.saveRefreshToken(refreshTokenEntity);

        // 3️⃣ 쿠키에 Refresh Token 저장
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // HTTPS 시 true
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(10); // 프론트에서 즉시 헤더 전환 로직 예정
        response.addCookie(refreshCookie);

        // 4️⃣ redirect_uri 쿠키 읽기
        String redirectUri = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("redirect_uri".equals(cookie.getName())) {
                    redirectUri = cookie.getValue();
                    cookie.setMaxAge(0); // 한 번 사용 후 삭제
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    break;
                }
            }
        }

        // 5️⃣ 리다이렉트 대상 설정
        if (redirectUri == null || redirectUri.isBlank()) {
            redirectUri = "http://localhost:5173"; // 기본 리다이렉트 URI (플랫폼 메인)
        }

        response.sendRedirect(redirectUri);
    }
}

