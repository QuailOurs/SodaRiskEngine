package com.soda.risk.engine.common.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RedisCacheServiceTest {

    private RedisCacheService cache;

    @BeforeEach
    void setUp() {
        cache = new RedisCacheService(new ObjectMapper(), null);
    }

    @Test
    void supportsStringCountersExpiryAndSetIfAbsentInMemory() {
        cache.set("text", "value");
        assertThat(cache.get("text")).isEqualTo("value");
        assertThat(cache.hasKey("text")).isTrue();
        assertThat(cache.increment("counter")).isEqualTo(1);
        assertThat(cache.increment("counter", 4)).isEqualTo(5);

        assertThat(cache.setIfAbsent("lock", "owner-1", 1, TimeUnit.MINUTES)).isTrue();
        assertThat(cache.setIfAbsent("lock", "owner-2", 1, TimeUnit.MINUTES)).isFalse();
        assertThat(cache.get("lock")).isEqualTo("owner-1");

        cache.set("expired", "value", 0, TimeUnit.MILLISECONDS);
        assertThat(cache.get("expired")).isNull();
        assertThat(cache.hasKey("expired")).isFalse();
    }

    @Test
    void supportsHashSetAndListOperationsWithRedisCompatibleDeletion() {
        cache.hSet("hash:1", "name", "Soda");
        cache.hSet("hash:1", "state", "ready");
        assertThat(cache.hGet("hash:1", "name")).isEqualTo("Soda");
        assertThat(cache.hMultiGet("hash:1", List.of("state", "missing")))
                .containsExactly("ready", null);

        assertThat(cache.sAdd("set:1", "a", "b", "a")).isEqualTo(2);
        assertThat(cache.sIsMember("set:1", "b")).isTrue();
        cache.replaceSet("set:1", Set.of("c"));
        assertThat(cache.sMembers("set:1")).containsExactly("c");

        assertThat(cache.lPush("list:1", "one")).isEqualTo(1);
        assertThat(cache.lPush("list:1", "two")).isEqualTo(2);
        assertThat(cache.lRange("list:1", 0, -1)).containsExactly("two", "one");
        assertThat(cache.lRange("list:1", 1, 1)).containsExactly("one");

        assertThat(cache.keys("*:1")).containsExactlyInAnyOrder("hash:1", "set:1", "list:1");
        assertThat(cache.delete(List.of("hash:1", "set:1", "list:1"))).isEqualTo(3);
        assertThat(cache.keys("*:1")).isEmpty();
    }

    @Test
    void expiresAndDeletesNonStringDataStructures() {
        cache.hSet("hash", "field", "value");
        assertThat(cache.expire("hash", 0, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(cache.hGetAll("hash")).isEmpty();

        cache.sAdd("set", "value");
        assertThat(cache.delete("set")).isTrue();
        assertThat(cache.hasKey("set")).isFalse();
        assertThat(cache.expire("missing", 1, TimeUnit.MINUTES)).isFalse();
    }

    @Test
    void serializesJsonObjectsAndGenericCollections() {
        cache.setJson("json:map", Map.of("name", "Soda", "version", 2));
        Map<String, Object> value = cache.getJson("json:map", new TypeReference<>() {});
        assertThat(value).containsEntry("name", "Soda").containsEntry("version", 2);

        cache.setJson("json:list", List.of("a", "b"), 1, TimeUnit.MINUTES);
        assertThat(cache.getJson("json:list", List.class)).containsExactly("a", "b");
    }
}
