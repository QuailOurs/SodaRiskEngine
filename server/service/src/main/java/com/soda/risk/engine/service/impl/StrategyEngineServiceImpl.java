package com.soda.risk.engine.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.risk.engine.api.dto.*;
import com.soda.risk.engine.api.interfaces.IStrategyEngineService;
import com.soda.risk.engine.common.enums.CodeEnum;
import com.soda.risk.engine.common.monitor.MonitorFacade;
import com.soda.risk.engine.service.engine.EngineEvaluationException;
import com.soda.risk.engine.service.engine.StrategyRuntimeEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 策略引擎服务实现
 * 整合计算引擎和处置流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyEngineServiceImpl implements IStrategyEngineService {

    private final StrategyRuntimeEngine runtimeEngine;
    private final ObjectMapper objectMapper;

    @Override
    public Response<EngineDecisionResult> evaluate(EngineEvaluateRequest request) {
        long start = System.currentTimeMillis();
        try {
            EngineDecisionResult result = runtimeEngine.evaluate(request);
            MonitorFacade.insert("[service]StrategyEvaluate", System.currentTimeMillis() - start);
            return Response.success(result);
        } catch (EngineEvaluationException e) {
            return Response.fail(e.getCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Strategy evaluation failed, sceneKey={}", request == null ? null : request.getSceneKey(), e);
            return Response.fail(CodeEnum.ENGINE_COMPUTE_ERROR);
        }
    }

    @Override
    public Response<java.util.List<EngineDecisionResult>> evaluateBatch(EngineBatchRequest request) {
        if (request == null || request.getRequests() == null || request.getRequests().isEmpty()) {
            return Response.fail(CodeEnum.PARAM_NULL);
        }
        java.util.List<EngineDecisionResult> results = new java.util.ArrayList<>();
        for (EngineEvaluateRequest item : request.getRequests()) {
            Response<EngineDecisionResult> one = evaluate(item);
            if (!one.isSuccess()) return Response.fail(one.getCode(), one.getMsg());
            results.add(one.getData());
        }
        return Response.success(results);
    }

    @Override
    public Response<StrategyHitResult> compute(String data, String sceneKey, String openKey) {
        try {
            java.util.Map<String, Object> input = objectMapper.readValue(data,
                    new TypeReference<java.util.Map<String, Object>>() {});
            Response<EngineDecisionResult> response = evaluate(EngineEvaluateRequest.builder()
                    .businessKey(openKey).sceneKey(sceneKey).data(input).needDetail(true).build());
            if (!response.isSuccess()) return Response.fail(response.getCode(), response.getMsg());
            EngineDecisionResult decision = response.getData();
            StrategyMatchResult first = !decision.getStrategies().isEmpty()
                    ? decision.getStrategies().get(0)
                    : !decision.getPreStrategies().isEmpty() ? decision.getPreStrategies().get(0) : null;
            return Response.success(StrategyHitResult.builder()
                    .strategyId(first == null ? 0L : first.getStrategyId())
                    .strategyName(first == null ? null : first.getStrategyName())
                    .strategyKey(first == null ? null : first.getStrategyKey())
                    .openId(openKey).sceneKey(sceneKey).hit(decision.isHit())
                    .hitRules(first == null ? java.util.List.of() : first.getRules())
                    .costMs(decision.getCostMs()).traceId(decision.getTraceId())
                    .extra(java.util.Map.of("decision", decision)).build());
        } catch (Exception e) {
            log.error("Strategy compute failed, sceneKey={}, openKey={}", sceneKey, openKey, e);
            return Response.fail(CodeEnum.DATA_PARSING_FAILED);
        }
    }

    @Override
    public Response<String> computeBatch(String data, String openKey) {
        try {
            java.util.List<EngineEvaluateRequest> requests = objectMapper.readValue(data,
                    new TypeReference<java.util.List<EngineEvaluateRequest>>() {});
            requests.forEach(request -> {
                if (request.getBusinessKey() == null || request.getBusinessKey().isBlank()) {
                    request.setBusinessKey(openKey);
                }
            });
            Response<java.util.List<EngineDecisionResult>> response = evaluateBatch(
                    EngineBatchRequest.builder().requests(requests).build());
            if (!response.isSuccess()) return Response.fail(response.getCode(), response.getMsg());
            return Response.success(objectMapper.writeValueAsString(response.getData()));
        } catch (Exception e) {
            return Response.fail(CodeEnum.DATA_PARSING_FAILED);
        }
    }
}
