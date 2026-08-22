package com.soda.risk.engine.core.strategy.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 字符串比较相关Aviator自定义函数集合
 * 包含: EQUAL_STRING, NOT_EQUAL_STRING, EQUAL_NULL_OBJECT, STARTS_WITH, END_WITH
 */
public class StringCompareFunctions {

    /**
     * 字符串等于操作符 - 忽略类型比较字符串值
     */
    @Component
    public static class EqualsStringOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String src = FunctionUtils.getStringValue(a, env);
            String dest = FunctionUtils.getStringValue(b, env);
            return AviatorBoolean.valueOf(StringUtils.equals(src, dest));
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.EQUAL_STRING.getValue();
        }
    }

    /**
     * 字符串不等于操作符
     */
    @Component
    public static class NotEqualsStringOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String src = FunctionUtils.getStringValue(a, env);
            String dest = FunctionUtils.getStringValue(b, env);
            return AviatorBoolean.valueOf(!StringUtils.equals(src, dest));
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.NOT_EQUAL_STRING.getValue();
        }
    }

    /**
     * 空对象判断操作符
     */
    @Component
    public static class EqualsNullObjectOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a) {
            Object src = FunctionUtils.getJavaObject(a, env);
            return AviatorBoolean.valueOf(src == null || "null".equals(String.valueOf(src)));
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.EQUAL_NULL_OBJECT.getValue();
        }
    }

    /**
     * 前缀匹配操作符
     */
    @Component
    public static class StartsWithOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String src = FunctionUtils.getStringValue(a, env);
            String[] destArr = StringUtils.split(FunctionUtils.getStringValue(b, env), ',');
            for (String dest : destArr) {
                if (StringUtils.startsWith(src, dest)) {
                    return AviatorBoolean.TRUE;
                }
            }
            return AviatorBoolean.FALSE;
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.STARTS_WITH.getValue();
        }
    }

    /**
     * 后缀匹配操作符
     */
    @Component
    public static class EndWithOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String src = FunctionUtils.getStringValue(a, env);
            String[] destArr = StringUtils.split(FunctionUtils.getStringValue(b, env), ',');
            for (String dest : destArr) {
                if (StringUtils.endsWith(src, dest)) {
                    return AviatorBoolean.TRUE;
                }
            }
            return AviatorBoolean.FALSE;
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.END_WITH.getValue();
        }
    }
}
