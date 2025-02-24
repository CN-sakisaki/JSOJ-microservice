package com.js.jsojbackenduserservice;

import com.js.jsojbackendcommon.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class JsojBackendUserServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testJwtGenerationAndValidation() {
        String token = JwtUtils.generateAccessToken(1L);
        System.out.println(token);
        assertTrue(JwtUtils.validateToken(token));
        Claims claims = JwtUtils.parseToken(token);
        System.out.println(claims.get("userId"));
    }
}
