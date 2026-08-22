package com.soda.risk.engine.core.strategy.feature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FeatureService单元测试
 */
class FeatureServiceTest {

    @Test
    void testFeatureHandlerInterface() {
        // Test that FeatureHandler interface defines required methods
        FeatureHandler handler = new FeatureHandler() {
            @Override
            public boolean supports(String featureType) {
                return "test".equals(featureType);
            }

            @Override
            public java.util.Map<String, Object> queryFeatures(java.util.Map<String, Object> dataMap,
                    java.util.List<java.util.Map.Entry<Object, Object>> featureConfigs) {
                return new java.util.HashMap<>();
            }

            @Override
            public Object querySingle(java.util.Map<String, Object> params) {
                return null;
            }
        };

        assertTrue(handler.supports("test"));
        assertFalse(handler.supports("other"));
        assertNotNull(handler.queryFeatures(new java.util.HashMap<>(), new java.util.ArrayList<>()));
    }
}
