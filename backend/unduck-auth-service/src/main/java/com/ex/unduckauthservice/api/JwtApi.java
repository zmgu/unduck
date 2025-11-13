package com.ex.unduckauthservice.api;

import com.ex.unduckauthservice.domain.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JwtApi {

    private final JwtService jwtService;

    /**
     * Refresh Token으로 Access Token 재발급
     * - HttpOnly 쿠키에서 Refresh Token 읽음
     * - 새 Access Token과 Refresh Token을 HttpOnly 쿠키로 발급
     */
    @PostMapping("/jwt/refresh")
    public ResponseEntity<Void> jwtRefreshApi(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        jwtService.refreshTokensViaCookie(request, response);
        return ResponseEntity.ok().build();
    }
}
