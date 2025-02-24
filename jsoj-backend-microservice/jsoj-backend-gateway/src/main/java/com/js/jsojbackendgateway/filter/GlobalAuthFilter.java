package com.js.jsojbackendgateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.js.jsojbackendcommon.constant.RedisConstant;
import com.js.jsojbackendcommon.utils.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 网关过滤器
 * @author sakisaki
 * @date 2025/2/22 14:52
 */
@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher matcher = new AntPathMatcher();

    private final StringRedisTemplate stringRedisTemplate;


    private final List<String> whiteList = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/swagger-ui/**",
            "/api/user/v2/api-docs"
    );

    public GlobalAuthFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // @Override
    // public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    //     ServerHttpRequest request = exchange.getRequest();
    //     ServerHttpResponse response = exchange.getResponse();
    //     String path = request.getURI().getPath();
    //     // 判断路径中是否包含inner,只允许内部调用
    //     if (matcher.match("/**/inner/**", path)) {
    //         response.setStatusCode(HttpStatus.FORBIDDEN);
    //         DataBufferFactory dataBufferFactory = response.bufferFactory();
    //         DataBuffer dataBuffer = dataBufferFactory.wrap(String.valueOf("无权限").getBytes(StandardCharsets.UTF_8));
    //         return response.writeWith(Mono.just(dataBuffer));
    //     }
    //     return chain.filter(exchange);
    // }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 拦截内部接口
        if (matcher.match("/**/inner/**", path)) {
            return unauthorizedResponse(exchange, "内部接口禁止访问");
        }

        // 2. 白名单放行
        for (String pattern : whiteList) {
            if (matcher.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        String token = extractToken(request);
        if (token == null) {
            return unauthorizedResponse(exchange, "未提供 Token");
        }
        try {
            // 4. 验证 Token 有效性
            if (!JwtUtils.validateToken(token)) {
                return unauthorizedResponse(exchange, "Token 无效");
            }

            // 5. 检查 Redis 中的 Token 状态
            String userId = JwtUtils.parseToken(token).getSubject();
            String redisToken = stringRedisTemplate.opsForValue().get(RedisConstant.TOKEN + RedisConstant.ACCESS_TOKEN + userId);
            if (!token.equals(redisToken)) {
                return unauthorizedResponse(exchange, "Token 已失效");
            }

            // 6. 权限校验（管理员才能访问 /api/admin/**）
            // String role = JwtUtils.parseToken(token).get("role", String.class);
            // if (path.startsWith("/api/admin") && !"admin".equals(role)) {
            //     return unauthorizedResponse(exchange, "权限不足");
            // }

            // 7. 传递用户信息到下游服务
            ServerHttpRequest newRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Exception e) {
            return unauthorizedResponse(exchange, "认证失败: " + e.getMessage());
        }
    }

    /**
     * 优先级最高
     * @return int
     */
    @Override
    public int getOrder() {
        return 0;
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        JSONObject json = new JSONObject();
        json.put("code", 401);
        json.put("message", message);
        DataBuffer buffer = response.bufferFactory().wrap(json.toJSONString().getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
