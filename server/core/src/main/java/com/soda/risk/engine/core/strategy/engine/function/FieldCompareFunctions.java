package com.soda.risk.engine.core.strategy.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 字段间比较相关Aviator自定义函数集合
 * 包含: FIELD_EQUAL, FIELD_NOT_EQUAL, FIELD_EXISTS, FIELD_NOT_EXISTS, FIELD_INCLUDE, FIELD_NOT_INCLUDE
 */
public class FieldCompareFunctions {

    /**
     * 字段相等 - 比较两个字段值是否相等
     */
    @Component
    public static class FieldEqualsOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String fieldA = FunctionUtils.getStringValue(a, env);
            String fieldB = FunctionUtils.getStringValue(b, env);
            if (env.containsKey(fieldA) && env.containsKey(fieldB)) {
                return AviatorBoolean.valueOf(String.valueOf(env.get(fieldA)).equals(String.valueOf(env.get(fieldB))));
            }
            return AviatorBoolean.FALSE;
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.FIELD_EQUAL.getValue();
        }
    }

    /**
     * 字段不相等
     */
    @Component
    public static class FieldNotEqualsOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String fieldA = FunctionUtils.getStringValue(a, env);
            String fieldB = FunctionUtils.getStringValue(b, env);
            if (env.containsKey(fieldA) && env.containsKey(fieldB)) {
                return AviatorBoolean.valueOf(!String.valueOf(env.get(fieldA)).equals(String.valueOf(env.get(fieldB))));
            }
            return AviatorBoolean.TRUE;
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.FIELD_NOT_EQUAL.getValue();
        }
    }

    /**
     * 字段存在
     */
    @Component
    public static class FieldExistsOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a) {
            String field = FunctionUtils.getStringValue(a, env);
            return AviatorBoolean.valueOf(env.containsKey(field) && env.get(field) != null);
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.FIELD_EXISTS.getValue();
        }
    }

    /**
     * 字段不存在
     */
    @Component
    public static class FieldNotExistsOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a) {
            String field = FunctionUtils.getStringValue(a, env);
            return AviatorBoolean.valueOf(!env.containsKey(field) || env.get(field) == null);
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.FIELD_NOT_EXISTS.getValue();
        }
    }

    /**
     * 字段包含 - 字段A的值包含字段B的值
     */
    @Component
    public static class FieldIncludeOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String srcField = FunctionUtils.getStringValue(a, env);
            String destField = FunctionUtils.getStringValue(b, env);
            if (env.containsKey(srcField)) {
                String src = String.valueOf(env.get(srcField));
                if (env.containsKey(destField)) {
                    String dest = String.valueOf(env.get(destField));
                    String[] destArr = dest.split(",");
                    for (String d : destArr) {
                        if (StringUtils.contains(src, d)) {
                            return AviatorBoolean.TRUE;
                        }
                    }
                }
            }
            return AviatorBoolean.FALSE;
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.FIELD_INCLUDE.getValue();
        }
    }

    /**
     * 字段不包含
     */
    @Component
    public static class FieldNotIncludeOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String srcField = FunctionUtils.getStringValue(a, env);
            String destField = FunctionUtils.getStringValue(b, env);
            if (env.containsKey(srcField)) {
                String src = String.valueOf(env.get(srcField));
                if (env.containsKey(destField)) {
                    String dest = String.valueOf(env.get(destField));
                    String[] destArr = dest.split(",");
                    for (String d : destArr) {
                        if (StringUtils.contains(src, d)) {
                            return AviatorBoolean.FALSE;
                        }
                    }
                }
            }
            return AviatorBoolean.TRUE;
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.FIELD_NOT_INCLUDE.getValue();
        }
    }
}
