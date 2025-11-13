package com.ex.unduckauthservice.api;

import com.ex.unduckauthservice.domain.jwt.service.JwtService;
import com.ex.unduckauthservice.handler.RefreshTokenLogoutHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 로그아웃 API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LogoutApi {

    private final JwtService jwtService;

    /**
     * 로그아웃
     * - Refresh Token Redis 삭제 (RefreshTokenLogoutHandler가 처리)
     * - 쿠키 삭제 (RefreshTokenLogoutHandler가 처리)
     * - SecurityContext 무효화
     *
     * @param redirectService 로그아웃 후 돌아갈 서비스명 (예: "game", "chat")
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestParam(value = "redirect_service", required = false, defaultValue = "platform") String redirectService,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication != null) {
            new RefreshTokenLogoutHandler(jwtService).logout(request, response, authentication);
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        String redirectUrl = buildRedirectUrl(redirectService);

        log.info("✅ 로그아웃 완료 - RedirectUrl: {}", redirectUrl);

        return ResponseEntity.ok(Map.of(
                "success", "true",
                "redirectUrl", redirectUrl
        ));
    }

    /**
     * redirect_service에 따라 리다이렉트 URL 생성
     */
    private String buildRedirectUrl(String redirectService) {
        if (redirectService == null || redirectService.isEmpty() || "platform".equals(redirectService)) {
            return "/platform/login";
        }

        // 서비스별 로그인 페이지
        return "/" + redirectService + "/login";
    }
}
