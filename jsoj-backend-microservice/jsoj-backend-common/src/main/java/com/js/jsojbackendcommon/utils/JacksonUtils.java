package com.js.jsojbackendcommon.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * Json与对象转换工具
 * @author sakisaki
 * @date 2025/3/4 17:16
 */
@Slf4j
public class JacksonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 通用方法：JSON 转 List<T>
    public static <T> List<T> toList(String json, Class<T> elementType) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    // 通用方法：JSON 转对象
    public static <T> T toObject(String json, Class<T> elementType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, elementType);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
