package com.soda.risk.engine.web.controller;

import com.soda.risk.engine.api.dto.*;
import com.soda.risk.engine.api.interfaces.IStrategyEngineService;
import com.soda.risk.engine.service.engine.StrategyRuntimeEngine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 面向接入方的Spring Boot HTTP决策接口。 */
@RestController
@RequestMapping("/api/v1/engine")
@RequiredArgsConstructor
public class EngineEvaluationController {

    private final IStrategyEngineService strategyEngineService;
    private final StrategyRuntimeEngine runtimeEngine;

    @PostMapping("/evaluate")
    public Response<EngineDecisionResult> evaluate(@Valid @RequestBody EngineEvaluateRequest request) {
        return strategyEngineService.evaluate(request);
    }

    @PostMapping("/evaluate/batch")
    public Response<List<EngineDecisionResult>> evaluateBatch(@Valid @RequestBody EngineBatchRequest request) {
        return strategyEngineService.evaluateBatch(request);
    }

    @GetMapping("/config/status")
    public Response<EngineConfigStatus> configStatus() {
        return Response.success(runtimeEngine.status());
    }

    @PostMapping("/config/reload")
    public Response<EngineConfigStatus> reloadConfig() {
        return Response.success(runtimeEngine.reload());
    }

    @GetMapping("/health")
    public Response<String> health() {
        return Response.success("OK");
    }
}
