package com.soda.risk.engine.web.controller;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.dto.StrategyHitResult;
import com.soda.risk.engine.api.interfaces.IStrategyEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 策略引擎REST控制器
 */
@RestController
@RequestMapping("/api/v1/strategy")
@RequiredArgsConstructor
public class StrategyEngineController {

    private final IStrategyEngineService strategyEngineService;

    /**
     * 策略计算
     */
    @PostMapping("/compute")
    public Response<StrategyHitResult> compute(@RequestParam String data,
                                                @RequestParam String sceneKey,
                                                @RequestParam String openKey) {
        return strategyEngineService.compute(data, sceneKey, openKey);
    }

    /**
     * 批量策略计算
     */
    @PostMapping("/compute/batch")
    public Response<String> computeBatch(@RequestParam String data,
                                          @RequestParam String openKey) {
        return strategyEngineService.computeBatch(data, openKey);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Response<String> health() {
        return Response.success("OK");
    }
}
