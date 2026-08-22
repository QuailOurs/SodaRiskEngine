package com.soda.risk.engine.core.strategy.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 不包含操作符
 */
@Component
public class NotIncludeOperatorFunction extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
        String src = FunctionUtils.getStringValue(a, env);
        String[] destArr = StringUtils.split(FunctionUtils.getStringValue(b, env), ',');
        for (String dest : destArr) {
            if (StringUtils.contains(src, dest)) {
                return AviatorBoolean.FALSE;
            }
        }
        return AviatorBoolean.TRUE;
    }

    @Override
    public String getName() {
        return ExpressionOperatorTypeEnum.NOT_INCLUDE.getValue();
    }
}
