package com.soda.risk.engine.core.strategy.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
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
        if (src == null || dest == null) return AviatorBoolean.FALSE;

        List<String> srcList = new ArrayList<>();
        if (src instanceof Collection<?> collection) {
            collection.forEach(value -> srcList.add(normalize(value)));
        } else if (src instanceof String text) {
            Arrays.stream(text.split(",")).map(IntersectionOperatorFunction::normalize).forEach(srcList::add);
        } else if (src.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(src); i++) srcList.add(normalize(Array.get(src, i)));
        } else {
            srcList.add(normalize(src));
        }
        List<String> destList = Arrays.stream(dest.split(","))
                .map(IntersectionOperatorFunction::normalize)
                .toList();

        return AviatorBoolean.valueOf(!CollectionUtils.intersection(srcList, destList).isEmpty());
    }

    @Override
    public String getName() {
        return ExpressionOperatorTypeEnum.INTERSECTION.getValue();
    }

    private static String normalize(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
