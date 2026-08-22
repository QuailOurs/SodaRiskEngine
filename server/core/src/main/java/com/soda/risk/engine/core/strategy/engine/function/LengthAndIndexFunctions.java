package com.soda.risk.engine.core.strategy.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 长度和指标合并相关Aviator自定义函数集合
 * 包含: LENGTH_MORE_THAN_AND, LENGTH_LESS_THAN_AND,
 *       INDEX_MERGE_MORE_THAN, INDEX_MERGE_LESS_THAN, INDEX_MERGE_EQUAL,
 *       INDEX_MERGE_MORE_EQUAL, INDEX_MERGE_LESS_EQUAL
 */
public class LengthAndIndexFunctions {

    /**
     * 字符串长度大于等于
     */
    @Component
    public static class LengthMoreThanAndOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String src = FunctionUtils.getStringValue(a, env);
            int len = FunctionUtils.getNumberValue(b, env).intValue();
            return AviatorBoolean.valueOf(src != null && src.length() >= len);
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.LENGTH_MORE_THAN_AND.getValue();
        }
    }

    /**
     * 字符串长度小于等于
     */
    @Component
    public static class LengthLessThanAndOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String src = FunctionUtils.getStringValue(a, env);
            int len = FunctionUtils.getNumberValue(b, env).intValue();
            return AviatorBoolean.valueOf(src != null && src.length() <= len);
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.LENGTH_LESS_THAN_AND.getValue();
        }
    }

    /**
     * 指标合并大于
     * 格式: INDEX_MERGE_MORE_THAN('field1,field2','threshold')
     * 将多个字段的值相加后与阈值比较
     */
    @Component
    public static class IndexMergeMoreThanOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String[] fields = StringUtils.split(FunctionUtils.getStringValue(a, env), ',');
            double threshold = Double.parseDouble(FunctionUtils.getStringValue(b, env));
            double sum = mergeIndex(fields, env);
            return AviatorBoolean.valueOf(sum > threshold);
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.INDEX_MERGE_MORE_THAN.getValue();
        }
    }

    /**
     * 指标合并小于
     */
    @Component
    public static class IndexMergeLessThanOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String[] fields = StringUtils.split(FunctionUtils.getStringValue(a, env), ',');
            double threshold = Double.parseDouble(FunctionUtils.getStringValue(b, env));
            double sum = mergeIndex(fields, env);
            return AviatorBoolean.valueOf(sum < threshold);
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.INDEX_MERGE_LESS_THAN.getValue();
        }
    }

    /**
     * 指标合并等于
     */
    @Component
    public static class IndexMergeEqualOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String[] fields = StringUtils.split(FunctionUtils.getStringValue(a, env), ',');
            double threshold = Double.parseDouble(FunctionUtils.getStringValue(b, env));
            double sum = mergeIndex(fields, env);
            return AviatorBoolean.valueOf(Double.compare(sum, threshold) == 0);
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.INDEX_MERGE_EQUAL.getValue();
        }
    }

    /**
     * 指标合并大于等于
     */
    @Component
    public static class IndexMergeMoreEqualOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String[] fields = StringUtils.split(FunctionUtils.getStringValue(a, env), ',');
            double threshold = Double.parseDouble(FunctionUtils.getStringValue(b, env));
            double sum = mergeIndex(fields, env);
            return AviatorBoolean.valueOf(sum >= threshold);
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.INDEX_MERGE_MORE_EQUAL.getValue();
        }
    }

    /**
     * 指标合并小于等于
     */
    @Component
    public static class IndexMergeLessEqualOperatorFunction extends AbstractFunction {
        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
            String[] fields = StringUtils.split(FunctionUtils.getStringValue(a, env), ',');
            double threshold = Double.parseDouble(FunctionUtils.getStringValue(b, env));
            double sum = mergeIndex(fields, env);
            return AviatorBoolean.valueOf(sum <= threshold);
        }

        @Override
        public String getName() {
            return ExpressionOperatorTypeEnum.INDEX_MERGE_LESS_EQUAL.getValue();
        }
    }

    private static double mergeIndex(String[] fields, Map<String, Object> env) {
        double sum = 0;
        for (String field : fields) {
            String trimmed = field.trim();
            if (env.containsKey(trimmed) && env.get(trimmed) != null) {
                try {
                    sum += Double.parseDouble(String.valueOf(env.get(trimmed)));
                } catch (NumberFormatException ignored) {
                    // non-numeric field treated as 0
                }
            }
        }
        return sum;
    }
}
