package com.soda.risk.engine.web.controller;

import com.soda.risk.engine.api.dto.DisposerResponse;
import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.interfaces.IDisposerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 处置引擎REST API
 */
@Tag(name = "处置引擎", description = "账号处置相关接口")
@RestController
@RequestMapping("/api/v1/disposer")
@RequiredArgsConstructor
public class DisposerController {

    private final IDisposerService disposerService;

    @Operation(summary = "执行处置", description = "根据策略执行账号处置（锁定/封禁/告警）")
    @PostMapping("/execute")
    public Response<DisposerResponse> execute(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "处置策略ID") @RequestParam String strategyId,
            @RequestBody(required = false) Map<String, Object> params) {
        DisposerResponse response = disposerService.execute(userId, strategyId, params != null ? params : Map.of());
        return response.isSuccess() ? Response.success(response) : Response.fail(-1, response.getMessage());
    }

    @Operation(summary = "解除处置", description = "解除用户的处置状态")
    @PostMapping("/release")
    public Response<DisposerResponse> release(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "处置类型") @RequestParam String disposerType) {
        DisposerResponse response = disposerService.release(userId, disposerType);
        return response.isSuccess() ? Response.success(response) : Response.fail(-1, response.getMessage());
    }

    @Operation(summary = "查询处置状态", description = "查询用户当前的处置状态")
    @GetMapping("/status/{userId}")
    public Response<Map<String, Object>> getStatus(@PathVariable String userId) {
        Map<String, Object> status = disposerService.queryStatus(userId);
        return Response.success(status);
    }
}
