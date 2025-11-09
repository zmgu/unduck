package com.ex.unduckauthservice.domain.jwt.service;

import com.ex.unduckauthservice.domain.jwt.dto.JWTResponseDTO;
import com.ex.unduckauthservice.domain.jwt.dto.RefreshTokenRequestDTO;
import com.ex.unduckauthservice.domain.jwt.entity.RefreshTokenEntity;
import com.ex.unduckauthservice.domain.jwt.repository.RefreshTokenRedisRepository;
import com.ex.unduckauthservice.domain.jwt.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    // 소셜 로그인 성공 후 쿠키(Refresh) -> 헤더 방식으로 응답
    public JWTResponseDTO cookie2Header(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        // 쿠키 리스트
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new RuntimeException("쿠키가 존재하지 않습니다.");
        }

        // Refresh 토큰 획득
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null) {
            throw new RuntimeException("refreshToken 쿠키가 없습니다.");
        }

        // Refresh 토큰 검증
        Boolean isValid = JWTUtil.isValid(refreshToken, false);
        if (!isValid) {
            throw new RuntimeException("유효하지 않은 refreshToken입니다.");
        }

        // 정보 추출
        String username = JWTUtil.getUsername(refreshToken);
        String role = JWTUtil.getRole(refreshToken);

        // 토큰 생성
        String newAccessToken = JWTUtil.createJWT(username, role, true);
        String newRefreshToken = JWTUtil.createJWT(username, role, false);


        refreshTokenRedisRepository.refreshTokenDelete(refreshToken);

        // 기존 쿠키 제거
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(10);
        response.addCookie(refreshCookie);

        return new JWTResponseDTO(newAccessToken, newRefreshToken);
    }

    public JWTResponseDTO refreshRotate(RefreshTokenRequestDTO dto) {

        String refreshToken = dto.getRefreshToken();

        // Refresh 토큰 검증
        Boolean isValid = JWTUtil.isValid(refreshToken, false);
        if (!isValid) {
            throw new RuntimeException("유효하지 않은 refreshToken입니다.");
        }

        // RefreshEntity 존재 확인 (화이트리스트)
        if (!existsRefreshToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 refreshToken입니다.");
        }

        // 정보 추출
        String username = JWTUtil.getUsername(refreshToken);
        String role = JWTUtil.getRole(refreshToken);

        // 토큰 생성
        String newAccessToken = JWTUtil.createJWT(username, role, true);
        String newRefreshToken = JWTUtil.createJWT(username, role, false);

        // 기존 Refresh 토큰 DB 삭제 후 신규 추가
        RefreshTokenEntity newRefreshEntity = RefreshTokenEntity.builder()
                .username(username)
                .refreshToken(newRefreshToken)
                .build();

        removeRefreshToken(refreshToken);
        saveRefreshToken(newRefreshEntity);

        return new JWTResponseDTO(newAccessToken, newRefreshToken);
    }

    public void saveRefreshToken(RefreshTokenEntity refreshTokenEntity) {
        refreshTokenRedisRepository.refreshTokenSave(refreshTokenEntity);
    }

    public Boolean existsRefreshToken(String refreshToken) {
        return refreshTokenRedisRepository.refreshTokenExists(refreshToken);
    }

    public void removeRefreshToken(String refreshToken) {
        refreshTokenRedisRepository.refreshTokenDelete(refreshToken);
    }

}
