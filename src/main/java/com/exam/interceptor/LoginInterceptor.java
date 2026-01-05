package com.exam.interceptor;

import com.exam.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final Map<String, User> tokenUserMap = new HashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (uri.contains("/login") || uri.contains("/register") || uri.contains("/check-username") || 
            uri.contains("/swagger") || uri.contains("/api-docs") || uri.contains("/doc.html") ||
            uri.contains("/webjars") || uri.contains("/favicon.ico") || uri.contains("/.well-known")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\",\"data\":null}");
            return false;
        }

        String token = authorization.substring(7);
        User user = tokenUserMap.get(token);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\",\"data\":null}");
            return false;
        }

        request.setAttribute("currentUser", user);
        return true;
    }
}
