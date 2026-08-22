package com.soda.risk.engine.core.strategy.rule;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 Aviator 执行并缓存规则表达式
 * 启动时自动注册所有自定义Aviator函数（INCLUDE/INTERSECTION/FIELD_EQUAL等）
 */
@Slf4j
@Component
public class RuleExpressionEvaluator implements InitializingBean {

    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();
    private final List<AbstractFunction> customFunctions;

    public RuleExpressionEvaluator(List<AbstractFunction> customFunctions) {
        this.customFunctions = customFunctions;
    }

    @Override
    public void afterPropertiesSet() {
        // 注册所有自定义Aviator函数
        if (customFunctions != null) {
            for (AbstractFunction function : customFunctions) {
                AviatorEvaluator.addFunction(function);
                log.debug("Registered Aviator function: {}", function.getName());
            }
            log.info("Registered {} custom Aviator functions", customFunctions.size());
        }
    }

    /**
     * 评估表达式
     * @param expression 表达式字符串
     * @param data 数据上下文
     * @return 是否匹配
     */
    public boolean evaluate(String expression, Map<String, Object> data) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        try {
            Expression compiledExpression = compileExpression(expression);
            Object result = compiledExpression.execute(data);
            return result != null && Boolean.TRUE.equals(result);

        } catch (Exception e) {
            log.error("Expression evaluate failed: {}", expression, e);
            return false;
        }
    }

    /**
     * 评估表达式并返回结果值
     */
    public Object evaluateValue(String expression, Map<String, Object> data) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        try {
            Expression compiledExpression = compileExpression(expression);
            return compiledExpression.execute(data);
        } catch (Exception e) {
            log.error("Expression evaluateValue failed: {}", expression, e);
            return null;
        }
    }

    /**
     * 编译表达式（带缓存）
     */
    public Expression compileExpression(String expression) {
        return expressionCache.computeIfAbsent(expression, expr -> {
            // 已缓存则直接返回
            if (AviatorEvaluator.getInstance().isExpressionCached(expr)) {
                return AviatorEvaluator.getInstance().getCachedExpression(expr);
            }
            return AviatorEvaluator.compile(expr, true);
        });
    }

    /**
     * 清除表达式缓存
     */
    public void clearCache() {
        expressionCache.clear();
    }

    /**
     * 销毁指定表达式的缓存
     */
    public void invalidateExpressionCache(String expression) {
        if (expression != null && !expression.trim().isEmpty()) {
            expressionCache.remove(expression);
            AviatorEvaluator.invalidateCache(expression);
        }
    }
}
