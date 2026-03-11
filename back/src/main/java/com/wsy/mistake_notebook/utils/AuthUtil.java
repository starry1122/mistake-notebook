package com.wsy.mistake_notebook.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class AuthUtil {

    private AuthUtil() {
    }

    public static String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth)) {
            if (auth.startsWith("Bearer ")) {
                return auth.substring(7).trim();
            }
            return auth.trim();
        }

        String token = request.getHeader("token");
        if (StringUtils.hasText(token)) {
            return token.trim();
        }
        return null;
    }

    public static Long requireStudentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            throw new IllegalStateException("\u672a\u767b\u5f55\uff1a\u7f3a\u5c11Token");
        }

        String role = JwtUtil.getRole(token);
        if (!"student".equals(role)) {
            throw new IllegalStateException("\u65e0\u6743\u9650\uff1a\u4ec5\u5b66\u751f\u53ef\u8bbf\u95ee");
        }
        return JwtUtil.getUserId(token);
    }

    public static Long requireAdminUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            throw new IllegalStateException("\u672a\u767b\u5f55\uff1a\u7f3a\u5c11Token");
        }

        String role = JwtUtil.getRole(token);
        if (!"admin".equals(role)) {
            throw new IllegalStateException("\u65e0\u6743\u9650\uff1a\u4ec5\u7ba1\u7406\u5458\u53ef\u8bbf\u95ee");
        }
        return JwtUtil.getUserId(token);
    }
}

