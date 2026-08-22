package com.soda.risk.engine.core.strategy.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数组交集操作符
 * 判断两个集合是否有交集
 */
@Component
public class IntersectionOperatorFunction extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject a, AviatorObject b) {
        Object src = FunctionUtils.getJavaObject(a, env);
        String dest = FunctionUtils.getStringValue(b, env);
        Object[] srcArr = new String[0];
        Object[] destArr = dest.split(",");

        if (src instanceof Collection) {
            srcArr = ((Collection<?>) src).toArray(new Object[0]);
        } else if (src instanceof String) {
            srcArr = ((String) src).split(",");
        } else if (src.getClass().isArray()) {
            if (src instanceof Object[]) {
                srcArr = (Object[]) src;
            }
        }

        List<Object> srcList = new ArrayList<>(Arrays.asList(srcArr));
        List<Object> destList = new ArrayList<>(Arrays.asList(destArr));

        return AviatorBoolean.valueOf(CollectionUtils.intersection(srcList, destList).size() > 0);
    }

    @Override
    public String getName() {
        return ExpressionOperatorTypeEnum.INTERSECTION.getValue();
    }
}
