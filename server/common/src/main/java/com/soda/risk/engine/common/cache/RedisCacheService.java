package com.soda.risk.engine.common.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务 - 统一封装Redis操作
 * 当Redis不可用时(dev环境)自动降级为内存Map
 */
@Slf4j
@Service
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final boolean redisAvailable;

    // 内存降级存储
    private final Map<String, String> memoryStore = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> memorySetStore = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> memoryHashStore = new ConcurrentHashMap<>();

    public RedisCacheService(ObjectMapper objectMapper,
                             @Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.redisAvailable = redisTemplate != null;
        if (!redisAvailable) {
            log.warn("Redis not available, using in-memory cache fallback (dev mode)");
        }
    }

    // ========== String操作 ==========

    public void set(String key, String value) {
        if (redisAvailable) {
            redisTemplate.opsForValue().set(key, value);
        } else {
            memoryStore.put(key, value);
        }
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        if (redisAvailable) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } else {
            memoryStore.put(key, value);
        }
    }

    public String get(String key) {
        if (redisAvailable) {
            return redisTemplate.opsForValue().get(key);
        }
        return memoryStore.get(key);
    }

    public Boolean delete(String key) {
        if (redisAvailable) {
            return redisTemplate.delete(key);
        }
        return memoryStore.remove(key) != null;
    }

    public Long delete(Collection<String> keys) {
        if (redisAvailable) {
            return redisTemplate.delete(keys);
        }
        long count = 0;
        for (String key : keys) {
            if (memoryStore.remove(key) != null) count++;
        }
        return count;
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (redisAvailable) {
            return redisTemplate.expire(key, timeout, unit);
        }
        return true; // no-op in memory mode
    }

    public Boolean hasKey(String key) {
        if (redisAvailable) {
            return redisTemplate.hasKey(key);
        }
        return memoryStore.containsKey(key);
    }

    // ========== Hash操作 ==========

    public void hSet(String key, String field, String value) {
        if (redisAvailable) {
            redisTemplate.opsForHash().put(key, field, value);
        } else {
            memoryHashStore.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(field, value);
        }
    }

    public Object hGet(String key, String field) {
        if (redisAvailable) {
            return redisTemplate.opsForHash().get(key, field);
        }
        Map<String, String> map = memoryHashStore.get(key);
        return map != null ? map.get(field) : null;
    }

    public Map<Object, Object> hGetAll(String key) {
        if (redisAvailable) {
            return redisTemplate.opsForHash().entries(key);
        }
        Map<String, String> map = memoryHashStore.get(key);
        if (map == null) return Collections.emptyMap();
        Map<Object, Object> result = new HashMap<>();
        result.putAll(map);
        return result;
    }

    public Long hDelete(String key, Object... fields) {
        if (redisAvailable) {
            return redisTemplate.opsForHash().delete(key, fields);
        }
        Map<String, String> map = memoryHashStore.get(key);
        if (map == null) return 0L;
        long count = 0;
        for (Object field : fields) {
            if (map.remove(String.valueOf(field)) != null) count++;
        }
        return count;
    }

    // ========== Set操作 ==========

    public Long sAdd(String key, String... values) {
        if (redisAvailable) {
            return redisTemplate.opsForSet().add(key, values);
        }
        Set<String> set = memorySetStore.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        long count = 0;
        for (String v : values) {
            if (set.add(v)) count++;
        }
        return count;
    }

    public Set<String> sMembers(String key) {
        if (redisAvailable) {
            return redisTemplate.opsForSet().members(key);
        }
        return memorySetStore.getOrDefault(key, Collections.emptySet());
    }

    public Boolean sIsMember(String key, String value) {
        if (redisAvailable) {
            return redisTemplate.opsForSet().isMember(key, value);
        }
        Set<String> set = memorySetStore.get(key);
        return set != null && set.contains(value);
    }

    public Long sRemove(String key, String... values) {
        if (redisAvailable) {
            return redisTemplate.opsForSet().remove(key, (Object[]) values);
        }
        Set<String> set = memorySetStore.get(key);
        if (set == null) return 0L;
        long count = 0;
        for (String v : values) {
            if (set.remove(v)) count++;
        }
        return count;
    }

    /**
     * 用给定值完整替换集合，避免配置同步后残留已经删除的关联。
     */
    public void replaceSet(String key, Collection<String> values) {
        if (redisAvailable) {
            redisTemplate.delete(key);
            if (values != null && !values.isEmpty()) {
                redisTemplate.opsForSet().add(key, values.toArray(new String[0]));
            }
            return;
        }

        if (values == null || values.isEmpty()) {
            memorySetStore.remove(key);
        } else {
            Set<String> replacement = ConcurrentHashMap.newKeySet();
            replacement.addAll(values);
            memorySetStore.put(key, replacement);
        }
    }

    // ========== 计数器操作 ==========

    public Long increment(String key) {
        if (redisAvailable) {
            return redisTemplate.opsForValue().increment(key);
        }
        synchronized (memoryStore) {
            String val = memoryStore.getOrDefault(key, "0");
            long newVal = Long.parseLong(val) + 1;
            memoryStore.put(key, String.valueOf(newVal));
            return newVal;
        }
    }

    public Long increment(String key, long delta) {
        if (redisAvailable) {
            return redisTemplate.opsForValue().increment(key, delta);
        }
        synchronized (memoryStore) {
            String val = memoryStore.getOrDefault(key, "0");
            long newVal = Long.parseLong(val) + delta;
            memoryStore.put(key, String.valueOf(newVal));
            return newVal;
        }
    }

    /**
     * 设置值（仅当key不存在时），带过期时间
     */
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        if (redisAvailable) {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
            return Boolean.TRUE.equals(result);
        }
        if (memoryStore.containsKey(key)) {
            return false;
        }
        memoryStore.put(key, value);
        return true;
    }

    // ========== List操作 ==========

    public Long lPush(String key, String value) {
        if (redisAvailable) {
            return redisTemplate.opsForList().leftPush(key, value);
        }
        return 1L; // simplified for memory mode
    }

    public List<String> lRange(String key, long start, long end) {
        if (redisAvailable) {
            return redisTemplate.opsForList().range(key, start, end);
        }
        return Collections.emptyList();
    }

    // ========== JSON对象操作 ==========

    public <T> void setJson(String key, T obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            set(key, json);
        } catch (Exception e) {
            log.error("Set JSON failed for key={}", key, e);
        }
    }

    public <T> void setJson(String key, T obj, long timeout, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            set(key, json, timeout, unit);
        } catch (Exception e) {
            log.error("Set JSON failed for key={}", key, e);
        }
    }

    public <T> T getJson(String key, Class<T> clazz) {
        try {
            String json = get(key);
            if (json == null) return null;
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Get JSON failed for key={}", key, e);
            return null;
        }
    }

    public <T> T getJson(String key, TypeReference<T> typeRef) {
        try {
            String json = get(key);
            if (json == null) return null;
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("Get JSON failed for key={}", key, e);
            return null;
        }
    }

    /**
     * 批量获取指定前缀的所有Key
     */
    public Set<String> keys(String pattern) {
        if (redisAvailable) {
            return redisTemplate.keys(pattern);
        }
        // 简化模式：返回所有匹配前缀的key
        String prefix = pattern.replace("*", "");
        Set<String> result = new HashSet<>();
        for (String key : memoryStore.keySet()) {
            if (key.startsWith(prefix)) {
                result.add(key);
            }
        }
        return result;
    }

    /**
     * 批量获取Hash中指定field列表的值
     */
    public List<Object> hMultiGet(String key, List<String> fields) {
        if (redisAvailable) {
            return redisTemplate.opsForHash().multiGet(key, new ArrayList<>(fields));
        }
        Map<String, String> map = memoryHashStore.get(key);
        if (map == null) return Collections.emptyList();
        List<Object> result = new ArrayList<>();
        for (String field : fields) {
            result.add(map.get(field));
        }
        return result;
    }
}
