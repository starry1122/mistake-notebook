package com.wsy.mistake_notebook.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    // 密钥（生产环境建议放到配置文件）
    private static final String SECRET = "mistake_notebook_secret_key";

    // 过期时间 24小时
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    /**
     * 生成 Token
     */
    public static String generateToken(Long userId, String role) {

        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 解析 Token
     */
    public static DecodedJWT verifyToken(String token) {

        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token);
    }

    /**
     * 获取 userId
     */
    public static Long getUserId(String token) {
        return verifyToken(token).getClaim("userId").asLong();
    }

    /**
     * 获取角色
     */
    public static String getRole(String token) {
        return verifyToken(token).getClaim("role").asString();
    }
}
