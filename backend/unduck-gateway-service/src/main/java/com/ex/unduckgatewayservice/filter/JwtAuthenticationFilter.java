package com.ex.unduckgatewayservice.filter;

import com.ex.unduckgatewayservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String token = null;

            // 1. Authorization 헤더에서 토큰 확인 (우선순위 1)
            if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7); // "Bearer " 제거
                }
            }

            // 2. 쿠키에서 Access Token 확인 (우선순위 2) ✅ 추가
            if (token == null) {
                HttpCookie accessTokenCookie = request.getCookies().getFirst("accessToken");
                if (accessTokenCookie != null) {
                    token = accessTokenCookie.getValue();
                }
            }

            // 3. 토큰이 없으면 인증 실패
            if (token == null) {
                log.warn("토큰이 없습니다. Path: {}", request.getPath());
                return onError(exchange, "토큰이 없습니다.", HttpStatus.UNAUTHORIZED);
            }

            // 4. JWT 토큰 검증
            try {
                if (!JwtUtil.isValidAccessToken(token)) {
                    log.warn("유효하지 않은 Access Token입니다.");
                    return onError(exchange, "유효하지 않은 Access Token입니다.", HttpStatus.UNAUTHORIZED);
                }

                // 5. 토큰에서 사용자 정보 추출
                String username = JwtUtil.getUsername(token);
                String role = JwtUtil.getRole(token);

                // 6. 다운스트림 서비스로 사용자 정보 전달 (커스텀 헤더 추가)
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Username", username)
                        .header("X-User-Role", role)
                        .build();

                log.info("✅ JWT 인증 성공 - Username: {}, Role: {}, Path: {}", username, role, request.getPath());

                // 7. 다음 필터로 전달
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("JWT 검증 중 오류 발생", e);
                return onError(exchange, "토큰 검증 중 오류가 발생했습니다.", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * 에러 응답 생성 (JSON 형식)
     */
    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonError = String.format("{\"error\":\"%s\"}", errorMessage);
        DataBuffer buffer = response.bufferFactory().wrap(jsonError.getBytes(StandardCharsets.UTF_8));

        log.warn("❌ JWT 인증 실패: {} (Status: {})", errorMessage, httpStatus.value());

        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // 필요시 설정 추가 가능 (예: 특정 role만 허용 등)
    }
}