package com.ex.unduckauthservice.config;

import com.ex.unduckauthservice.domain.jwt.service.JwtService;
import com.ex.unduckauthservice.domain.oauth2.repository.OAuth2StateRedisRepository;
import com.ex.unduckauthservice.domain.oauth2.resolver.CustomOAuth2AuthorizationRequestResolver;
import com.ex.unduckauthservice.domain.user.entity.UserRoleType;
import com.ex.unduckauthservice.filter.GatewayAuthenticationFilter;
import com.ex.unduckauthservice.filter.LoginFilter;
import com.ex.unduckauthservice.handler.RefreshTokenLogoutHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationSuccessHandler loginSuccessHandler;
    private final AuthenticationSuccessHandler socialSuccessHandler;
    private final JwtService jwtService;
    private final OAuth2StateRedisRepository oAuth2StateRedisRepository;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(
            AuthenticationConfiguration authenticationConfiguration,
            @Qualifier("LoginSuccessHandler") AuthenticationSuccessHandler loginSuccessHandler,
            @Qualifier("SocialSuccessHandler") AuthenticationSuccessHandler socialSuccessHandler,
            JwtService jwtService,
            OAuth2StateRedisRepository oAuth2StateRedisRepository,
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.loginSuccessHandler = loginSuccessHandler;
        this.socialSuccessHandler = socialSuccessHandler;
        this.jwtService = jwtService;
        this.oAuth2StateRedisRepository = oAuth2StateRedisRepository;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withRolePrefix("ROLE_")
                .role(UserRoleType.ADMIN.name()).implies(UserRoleType.USER.name())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver() {
        return new CustomOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                oAuth2StateRedisRepository
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 보안 필터 ,기본 Form 기반 인증 필터들 ,기본 Basic 인증 필터 disable
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        // CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/never-use-this-url")
                .deleteCookies("accessToken", "refreshToken")
                .addLogoutHandler(new RefreshTokenLogoutHandler(jwtService))
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                })
        );

        // OAuth2 인증용 (커스텀 Resolver 적용)
        http.oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                        .authorizationRequestResolver(customOAuth2AuthorizationRequestResolver())
                )
                .successHandler(socialSuccessHandler));

        // 인가
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/jwt/refresh").permitAll() // ✅ /jwt/exchange 제거
                .requestMatchers(HttpMethod.POST, "/user/exist", "/user").permitAll()
                .requestMatchers(HttpMethod.GET, "/user").hasRole(UserRoleType.USER.name())
                .requestMatchers(HttpMethod.PUT, "/user").hasRole(UserRoleType.USER.name())
                .requestMatchers(HttpMethod.DELETE, "/user").hasRole(UserRoleType.USER.name())
                .anyRequest().authenticated()
        );

        // 예외 처리
        http.exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                })
                .accessDeniedHandler((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                })
        );

        // Gateway에서 전달된 사용자 정보로 SecurityContext 설정하는 필터 추가
        http.addFilterBefore(new GatewayAuthenticationFilter(), LogoutFilter.class);

        // 커스텀 로그인 필터 추가
        http.addFilterBefore(
                new LoginFilter(authenticationManager(authenticationConfiguration), loginSuccessHandler),
                UsernamePasswordAuthenticationFilter.class
        );

        // 세션 필터 설정
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }
}