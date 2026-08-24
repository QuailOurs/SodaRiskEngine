package com.soda.risk.engine.core.strategy.complement;

import java.util.Map;

/** 可插拔的数据补全处理器；实现只返回新增或覆盖字段，不直接修改原始请求。 */
public interface DataComplementHandler {

    String name();

    boolean supports(String sceneKey, Map<String, Object> data);

    Map<String, Object> complement(String sceneKey, Map<String, Object> data);
}
