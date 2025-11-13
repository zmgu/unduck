package com.ex.unduckauthservice.handler;

import com.ex.unduckauthservice.domain.jwt.service.JwtService;
import com.ex.unduckauthservice.domain.oauth2.repository.OAuth2StateRedisRepository;
import com.ex.unduckauthservice.util.CookieUtil;
import com.ex.unduckauthservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component("SocialSuccessHandler")
@RequiredArgsConstructor
public class SocialSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final OAuth2StateRedisRepository oAuth2StateRedisRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;

        // ✅ UserService와 동일한 방식으로 username 생성
        String registrationId = oauth2Token.getAuthorizedClientRegistrationId().toUpperCase();
        String username;

        if ("GOOGLE".equals(registrationId)) {
            // Google: sub 사용
            username = registrationId + "_" + oAuth2User.getAttribute("sub");
        } else if ("NAVER".equals(registrationId)) {
            // Naver: response 안의 id 사용
            Map<String, Object> naverResponse = oAuth2User.getAttribute("response");
            username = registrationId + "_" + naverResponse.get("id");
        } else {
            throw new RuntimeException("지원하지 않는 소셜 로그인입니다.");
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();

        log.info("✅ 소셜 로그인 성공 - Username: {}, Role: {}", username, role);

        // 1️⃣ Access Token + Refresh Token 생성
        String accessToken = JwtUtil.createJWT(username, role, true);
        String refreshToken = JwtUtil.createJWT(username, role, false);

        // 2️⃣ Refresh Token을 Redis에 저장
        jwtService.saveRefreshToken(refreshToken, username);

        // 3️⃣ HttpOnly 쿠키로 두 토큰 저장
        CookieUtil.addSecureCookie(response, "accessToken", accessToken, 3600); // 1시간
        CookieUtil.addSecureCookie(response, "refreshToken", refreshToken, 604800); // 7일

        // 4️⃣ OAuth2 state에서 redirect_service 조회
        String state = extractStateFromRequest(request);
        String redirectService = oAuth2StateRedisRepository.getRedirectService(state).orElse("");

        // 5️⃣ Redis에서 state 삭제 (일회용)
        if (!state.isEmpty()) {
            oAuth2StateRedisRepository.deleteState(state);
        }

        // 6️⃣ 리다이렉트 URL 생성
        String redirectUrl = buildRedirectUrl(redirectService);

        log.info("리다이렉트 URL: {}", redirectUrl);

        // 7️⃣ 프론트엔드로 리다이렉트
        response.sendRedirect(redirectUrl);
    }

    /**
     * HttpServletRequest에서 OAuth2 state 추출
     * OAuth2AuthenticationToken은 state 정보를 직접 제공하지 않으므로
     * HttpSession 또는 AuthorizationRequestRepository를 통해 가져와야 함
     */
    private String extractStateFromRequest(HttpServletRequest request) {
        // OAuth2 인증 과정에서 저장된 state를 세션에서 가져옴

        // 방법 1: 세션에서 가져오기 (간단한 방법)
        Object stateObj = request.getSession().getAttribute("oauth2_authorization_request_state");
        if (stateObj != null) {
            // 세션에서 state 삭제 (일회용)
            request.getSession().removeAttribute("oauth2_authorization_request_state");
            return stateObj.toString();
        }

        // 방법 2: 쿼리 파라미터에서 직접 가져오기 (fallback)
        String state = request.getParameter("state");
        if (state != null) {
            return state;
        }

        // state를 찾을 수 없으면 빈 문자열 반환 (플랫폼 메인으로)
        log.warn("OAuth2 state를 찾을 수 없습니다. 플랫폼 메인으로 리다이렉트합니다.");
        return "";
    }

    // 서비스별 메인 페이지로 리다이렉트 URL 생성
    private String buildRedirectUrl(String redirectService) {
        String frontendBaseUrl = "http://localhost:5173";

        if (redirectService == null || redirectService.isEmpty()) {
            return frontendBaseUrl + "/platform"; // 플랫폼 메인
        }

        // 서비스별 메인 페이지
        return frontendBaseUrl + "/" + redirectService;
    }
}
