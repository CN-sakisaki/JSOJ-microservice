package com.js.jsojbackendcommon.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

import static com.js.jsojbackendcommon.constant.JwtConstant.*;

/**
 * JWT工具类
 * @author sakisaki
 * @date 2025/2/23 13:46
 */
public class JwtUtils {
    // 生成 AccessToken
    public static String generateAccessToken(long userId) {
        return Jwts.builder()
                // 用户ID作为主体
                .setSubject(String.valueOf(userId))
                // 签发时间
                .setIssuedAt(new Date())
                // 过期时间
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                // 签名
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // 生成 RefreshToken（存入 Redis）
    public static String generateRefreshToken(long userId) {
        return Jwts.builder()
                // 用户ID作为主体
                .setSubject(String.valueOf(userId))
                // 签发时间
                .setIssuedAt(new Date())
                // 过期时间
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                // 签名
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // 验证 Token
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 解析 Token
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                // 设置签名密钥
                .setSigningKey(SECRET_KEY)
                .build()
                // 解析 Token
                .parseClaimsJws(token)
                .getBody();
    }
}
