package com.ex.unduckauthservice.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);
        return customizeRequest(authRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeRequest(authRequest, request);
    }

    private OAuth2AuthorizationRequest customizeRequest(OAuth2AuthorizationRequest req, HttpServletRequest request) {
        if (req == null) return null;

        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri != null) {
            // redirect_uri를 세션에 저장
            Cookie cookie = new Cookie("redirect_uri", redirectUri);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(180); // 3분 유지
            request.getSession().setAttribute("redirect_uri", redirectUri);
        }
        return req;
    }
}

