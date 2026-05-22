// AdminPinInterceptor.java — /admin/** 접근을 PIN 쿠키로 보호
package dev.sybaek.mukja.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminPinInterceptor implements HandlerInterceptor {
    private final String pin;
    public AdminPinInterceptor(String pin) { this.pin = pin; }

    // admin 쿠키 값이 설정 PIN과 일치하지 않으면 로그인으로 리다이렉트한다
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();
        if (uri.startsWith("/admin/login") || uri.equals("/admin/auth")) return true;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("admin".equals(c.getName()) && pin.equals(c.getValue())) return true;
            }
        }
        res.sendRedirect("/admin/login");
        return false;
    }
}
