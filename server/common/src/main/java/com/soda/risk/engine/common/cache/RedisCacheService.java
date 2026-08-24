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
import java.util.regex.Pattern;

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
    private final Map<String, Deque<String>> memoryListStore = new ConcurrentHashMap<>();
    private final Map<String, Long> memoryExpiryStore = new ConcurrentHashMap<>();

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
            memoryExpiryStore.remove(key);
        }
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        if (redisAvailable) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } else {
            memoryStore.put(key, value);
            memoryExpiryStore.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
        }
    }

    public String get(String key) {
        if (redisAvailable) {
            return redisTemplate.opsForValue().get(key);
        }
        evictIfExpired(key);
        return memoryStore.get(key);
    }

    public Boolean delete(String key) {
        if (redisAvailable) {
            return redisTemplate.delete(key);
        }
        return removeMemoryKey(key);
    }

    public Long delete(Collection<String> keys) {
        if (redisAvailable) {
            return redisTemplate.delete(keys);
        }
        return keys.stream().filter(key -> Boolean.TRUE.equals(delete(key))).count();
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (redisAvailable) {
            return redisTemplate.expire(key, timeout, unit);
        }
        evictIfExpired(key);
        if (!memoryKeyExists(key)) return false;
        memoryExpiryStore.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
        return true;
    }

    public Boolean hasKey(String key) {
        if (redisAvailable) {
            return redisTemplate.hasKey(key);
        }
        evictIfExpired(key);
        return memoryKeyExists(key);
    }

    // ========== Hash操作 ==========

    public void hSet(String key, String field, String value) {
        if (redisAvailable) {
            redisTemplate.opsForHash().put(key, field, value);
        } else {
            evictIfExpired(key);
            memoryHashStore.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(field, value);
        }
    }

    public Object hGet(String key, String field) {
        if (redisAvailable) {
            return redisTemplate.opsForHash().get(key, field);
        }
        evictIfExpired(key);
        Map<String, String> map = memoryHashStore.get(key);
        return map != null ? map.get(field) : null;
    }

    public Map<Object, Object> hGetAll(String key) {
        if (redisAvailable) {
            return redisTemplate.opsForHash().entries(key);
        }
        evictIfExpired(key);
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
        evictIfExpired(key);
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
        evictIfExpired(key);
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
        evictIfExpired(key);
        Set<String> values = memorySetStore.get(key);
        return values == null ? Collections.emptySet() : new HashSet<>(values);
    }

    public Boolean sIsMember(String key, String value) {
        if (redisAvailable) {
            return redisTemplate.opsForSet().isMember(key, value);
        }
        evictIfExpired(key);
        Set<String> set = memorySetStore.get(key);
        return set != null && set.contains(value);
    }

    public Long sRemove(String key, String... values) {
        if (redisAvailable) {
            return redisTemplate.opsForSet().remove(key, (Object[]) values);
        }
        evictIfExpired(key);
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
            memoryExpiryStore.remove(key);
        } else {
            Set<String> replacement = ConcurrentHashMap.newKeySet();
            replacement.addAll(values);
            memorySetStore.put(key, replacement);
            memoryExpiryStore.remove(key);
        }
    }

    // ========== 计数器操作 ==========

    public Long increment(String key) {
        if (redisAvailable) {
            return redisTemplate.opsForValue().increment(key);
        }
        synchronized (memoryStore) {
            evictIfExpired(key);
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
            evictIfExpired(key);
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
        synchronized (memoryStore) {
            evictIfExpired(key);
            if (memoryKeyExists(key)) return false;
            memoryStore.put(key, value);
            memoryExpiryStore.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
            return true;
        }
    }

    // ========== List操作 ==========

    public Long lPush(String key, String value) {
        if (redisAvailable) {
            return redisTemplate.opsForList().leftPush(key, value);
        }
        evictIfExpired(key);
        Deque<String> values = memoryListStore.computeIfAbsent(key, ignored -> new java.util.concurrent.ConcurrentLinkedDeque<>());
        values.addFirst(value);
        return (long) values.size();
    }

    public List<String> lRange(String key, long start, long end) {
        if (redisAvailable) {
            return redisTemplate.opsForList().range(key, start, end);
        }
        evictIfExpired(key);
        Deque<String> values = memoryListStore.get(key);
        if (values == null || values.isEmpty()) return Collections.emptyList();
        List<String> snapshot = new ArrayList<>(values);
        int size = snapshot.size();
        int from = normalizeListIndex(start, size);
        int to = normalizeListIndex(end, size);
        if (from >= size || to < 0 || from > to) return Collections.emptyList();
        from = Math.max(0, from);
        to = Math.min(size - 1, to);
        return new ArrayList<>(snapshot.subList(from, to + 1));
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
        String regex = Arrays.stream(pattern.split("\\*", -1))
                .map(Pattern::quote)
                .collect(java.util.stream.Collectors.joining(".*"));
        Pattern compiled = Pattern.compile("^" + regex + "$");
        Set<String> result = new HashSet<>();
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(memoryStore.keySet());
        allKeys.addAll(memoryHashStore.keySet());
        allKeys.addAll(memorySetStore.keySet());
        allKeys.addAll(memoryListStore.keySet());
        for (String key : allKeys) {
            evictIfExpired(key);
            if (memoryKeyExists(key) && compiled.matcher(key).matches()) result.add(key);
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
        evictIfExpired(key);
        Map<String, String> map = memoryHashStore.get(key);
        if (map == null) return Collections.emptyList();
        List<Object> result = new ArrayList<>();
        for (String field : fields) {
            result.add(map.get(field));
        }
        return result;
    }

    private boolean memoryKeyExists(String key) {
        return memoryStore.containsKey(key) || memoryHashStore.containsKey(key)
                || memorySetStore.containsKey(key) || memoryListStore.containsKey(key);
    }

    private boolean removeMemoryKey(String key) {
        boolean removed = memoryStore.remove(key) != null;
        removed |= memoryHashStore.remove(key) != null;
        removed |= memorySetStore.remove(key) != null;
        removed |= memoryListStore.remove(key) != null;
        memoryExpiryStore.remove(key);
        return removed;
    }

    private void evictIfExpired(String key) {
        Long expiresAt = memoryExpiryStore.get(key);
        if (expiresAt != null && expiresAt <= System.currentTimeMillis()) removeMemoryKey(key);
    }

    private int normalizeListIndex(long index, int size) {
        long normalized = index < 0 ? size + index : index;
        if (normalized < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        if (normalized > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) normalized;
    }
}
