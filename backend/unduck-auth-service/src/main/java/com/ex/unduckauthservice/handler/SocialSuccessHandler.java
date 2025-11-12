package com.ex.unduckauthservice.handler;

import com.ex.unduckauthservice.domain.jwt.service.JwtService;
import com.ex.unduckauthservice.util.CookieUtil;
import com.ex.unduckauthservice.util.JwtUtil;
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

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        System.out.println("✅ SocialSuccessHandler invoked for user: " + username);

        // Refresh Token 발급
        String refreshToken = JwtUtil.createJWT(username, role, false);

        jwtService.saveRefreshToken(refreshToken, username);

        // RefreshToken 쿠키 저장
        CookieUtil.addCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 14);

        // ✅ 항상 /cookie 로 리다이렉트
        String redirectUri = "http://localhost:5173/cookie";
        response.sendRedirect(redirectUri);
    }
}

