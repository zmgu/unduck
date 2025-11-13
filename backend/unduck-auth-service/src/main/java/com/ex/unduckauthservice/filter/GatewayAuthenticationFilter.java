package com.ex.unduckauthservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Gateway에서 JWT 검증 후 전달한 사용자 정보를 SecurityContext에 설정하는 필터
 */
@Slf4j
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Gateway에서 전달된 사용자 정보 헤더 확인
        String username = request.getHeader("X-User-Username");
        String role = request.getHeader("X-User-Role");

        // Gateway를 통해 들어온 요청이라면 SecurityContext 설정
        if (username != null && role != null) {

            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("✅ Gateway로부터 인증 정보 설정 완료 - Username: {}, Role: {}", username, role);
        }

        filterChain.doFilter(request, response);
    }
}
