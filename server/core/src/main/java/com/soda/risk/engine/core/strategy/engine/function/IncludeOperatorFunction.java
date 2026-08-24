package com.soda.risk.engine.core.strategy.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 包含操作符
 * 判断字段值是否包含指定子串
 */
@Component
public class IncludeOperatorFunction extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
        String src = FunctionUtils.getStringValue(a, env);
        String[] destArr = StringUtils.split(FunctionUtils.getStringValue(b, env), ',');
        if (src == null || destArr == null) return AviatorBoolean.FALSE;
        for (String dest : destArr) {
            if (StringUtils.contains(src, dest)) {
                return AviatorBoolean.TRUE;
            }
        }
        return AviatorBoolean.FALSE;
    }

    @Override
    public String getName() {
        return ExpressionOperatorTypeEnum.INCLUDE.getValue();
    }
}
