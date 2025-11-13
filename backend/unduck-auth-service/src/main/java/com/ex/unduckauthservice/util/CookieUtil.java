package com.ex.unduckauthservice.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

public final class CookieUtil {

    private CookieUtil() {} // 인스턴스화 방지

    //HttpOnly 쿠키 추가 (CSRF 방어를 위한 SameSite 속성 포함)
    public static void addSecureCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS 사용 시 true로 변경
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax"); // CSRF 방어
        response.addCookie(cookie);
    }

    // 일반 쿠키 추가 (HttpOnly 없음 - 특수한 경우에만 사용)
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    // 쿠키 삭제
    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    // 쿠키 조회
    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}