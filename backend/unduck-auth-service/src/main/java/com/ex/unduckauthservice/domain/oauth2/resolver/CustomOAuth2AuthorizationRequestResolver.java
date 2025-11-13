package com.ex.unduckauthservice.domain.oauth2.resolver;

import com.ex.unduckauthservice.domain.oauth2.repository.OAuth2StateRedisRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.UUID;

/**
 * OAuth2 인증 요청 시 redirect_service 정보를 state에 저장하는 Resolver
 */
@RequiredArgsConstructor
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;
    private final OAuth2StateRedisRepository oAuth2StateRedisRepository;

    public CustomOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2StateRedisRepository oAuth2StateRedisRepository
    ) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
        this.oAuth2StateRedisRepository = oAuth2StateRedisRepository;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    /**
     * Authorization Request에 커스텀 state 추가 및 Redis 저장
     */
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request
    ) {
        if (authorizationRequest == null) {
            return null;
        }

        // 1. 쿼리 파라미터에서 redirect_service 추출
        String redirectService = request.getParameter("redirect_service");
        if (redirectService == null || redirectService.isEmpty()) {
            redirectService = ""; // 기본값: 플랫폼 메인
        }

        // 2. 고유한 state 생성
        String customState = UUID.randomUUID().toString();

        // 3. Redis에 state와 redirect_service 매핑 저장
        oAuth2StateRedisRepository.saveState(customState, redirectService);

        // 4. ✅ 세션에도 state 저장 (SocialSuccessHandler에서 사용)
        request.getSession().setAttribute("oauth2_authorization_request_state", customState);

        // 5. Authorization Request에 커스텀 state 적용
        return OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .state(customState)
                .build();
    }
}