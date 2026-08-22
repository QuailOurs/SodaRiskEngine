package com.soda.risk.engine.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 通用工具类 - 合并原各模块重复的CommonUtils/CommonBeanUtils
 */
@Slf4j
public final class CommonUtils {

    private CommonUtils() {}

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 生成唯一标识
     */
    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取服务器IP
     */
    public static String getServerIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 对象转Map
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) return Collections.emptyMap();
        try {
            return OBJECT_MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("objectToMap failed", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Map转对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null || map.isEmpty()) return null;
        try {
            return OBJECT_MAPPER.convertValue(map, clazz);
        } catch (Exception e) {
            log.error("mapToObject failed", e);
            return null;
        }
    }

    /**
     * JSON字符串转Map
     */
    public static Map<String, Object> jsonToMap(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyMap();
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("jsonToMap failed", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("toJson failed", e);
            return null;
        }
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 安全的字符串比较
     */
    public static boolean equals(String a, String b) {
        return Objects.equals(a, b);
    }

    /**
     * 复制Bean属性（非null值）
     */
    public static void copyProperties(Object source, Object target, String... ignoreFields) {
        if (source == null || target == null) return;
        Set<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreFields));
        Field[] fields = source.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (ignoreSet.contains(field.getName())) continue;
            try {
                field.setAccessible(true);
                Object value = field.get(source);
                if (value != null) {
                    Field targetField = target.getClass().getDeclaredField(field.getName());
                    targetField.setAccessible(true);
                    targetField.set(target, value);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
