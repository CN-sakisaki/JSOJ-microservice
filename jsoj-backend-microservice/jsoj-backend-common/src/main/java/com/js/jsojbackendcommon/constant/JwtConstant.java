package com.js.jsojbackendcommon.constant;

import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * jwt相关常量
 * @author sakisaki
 * @date 2025/2/23 13:49
 */
public interface JwtConstant {
    // 长度至少32字节
    String SECRET_STRING = "MySecureKeyMySecureKeyMySecureKey";
    Key SECRET_KEY = new SecretKeySpec(
            SECRET_STRING.getBytes(StandardCharsets.UTF_8),
            SignatureAlgorithm.HS256.getJcaName()
    );
    // 安全的密钥
    // Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // AccessToken 有效期1天
    long ACCESS_TOKEN_EXPIRE = 24 * 60 * 60 * 1000;
    // RefreshToken 有效期7 天
    long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60 * 1000;
}
