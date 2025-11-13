package com.ex.unduckauthservice.handler;

import com.ex.unduckauthservice.domain.jwt.service.JwtService;
import com.ex.unduckauthservice.util.CookieUtil;
import com.ex.unduckauthservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component("LoginSuccessHandler")
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        log.info("✅ 일반 로그인 성공 - Username: {}, Role: {}", username, role);

        // 1️⃣ Access Token + Refresh Token 생성
        String accessToken = JwtUtil.createJWT(username, role, true);
        String refreshToken = JwtUtil.createJWT(username, role, false);

        // 2️⃣ Refresh Token을 Redis에 저장
        jwtService.saveRefreshToken(refreshToken, username);

        // 3️⃣ HttpOnly 쿠키로 두 토큰 저장
        CookieUtil.addSecureCookie(response, "accessToken", accessToken, 3600); // 1시간
        CookieUtil.addSecureCookie(response, "refreshToken", refreshToken, 604800); // 7일

        // 4️⃣ 쿼리 파라미터에서 redirect_service 추출
        String redirectService = request.getParameter("redirect_service");
        String redirectUrl = buildRedirectUrl(redirectService);

        log.info("리다이렉트 URL: {}", redirectUrl);

        // 5️⃣ JSON 응답 (프론트엔드에서 리다이렉트 처리)
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(
                String.format("{\"success\":true,\"redirectUrl\":\"%s\"}", redirectUrl)
        );
    }

    // 서비스별 메인 페이지로 리다이렉트 URL 생성
    private String buildRedirectUrl(String redirectService) {
        if (redirectService == null || redirectService.isEmpty()) {
            return "/platform";
        }

        return "/" + redirectService;
    }
}



