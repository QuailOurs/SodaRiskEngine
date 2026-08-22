package com.soda.risk.engine.api.interfaces;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.dto.EngineBatchRequest;
import com.soda.risk.engine.api.dto.EngineDecisionResult;
import com.soda.risk.engine.api.dto.EngineEvaluateRequest;
import com.soda.risk.engine.api.dto.StrategyHitResult;

import java.util.List;

/**
 * 策略引擎服务接口
 * 改为标准REST/Feign接口
 */
public interface IStrategyEngineService {

    Response<EngineDecisionResult> evaluate(EngineEvaluateRequest request);

    Response<List<EngineDecisionResult>> evaluateBatch(EngineBatchRequest request);

    /**
     * 策略计算 - 实时
     * @param data 请求数据JSON
     * @param sceneKey 场景标识
     * @param openKey 业务方Key
     * @return 策略命中结果
     */
    Response<StrategyHitResult> compute(String data, String sceneKey, String openKey);

    /**
     * 策略计算 - 批量
     * @param data 请求数据JSON
     * @param openKey 业务方Key
     * @return 策略命中结果列表
     */
    Response<String> computeBatch(String data, String openKey);
}
