package com.ex.unduckauthservice.handler;

import com.ex.unduckauthservice.domain.jwt.service.JwtService;
import com.ex.unduckauthservice.util.CookieUtil;
import com.ex.unduckauthservice.util.JwtUtil;
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
@Qualifier("LoginSuccessHandler")
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // 1️⃣ Refresh Token 발급
        String refreshToken = JwtUtil.createJWT(username, role, false);

        jwtService.saveRefreshToken(refreshToken, username);

        // 2️⃣ RefreshToken 쿠키 저장
        CookieUtil.addCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 14);


        // ✅ 3️⃣ JSON 응답으로 React 쪽 cookie 페이지로 이동하도록 지정
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"redirectUri\":\"/cookie\"}");
    }
}


