package com.soda.risk.engine.web.controller;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.dto.RiskDecisionResult;
import com.soda.risk.engine.api.interfaces.IRiskDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 风险决策REST控制器
 */
@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskDecisionController {

    private final IRiskDecisionService riskDecisionService;

    /**
     * 风险识别
     */
    @PostMapping("/identification")
    public Response<RiskDecisionResult> riskIdentification(@RequestParam String data,
                                                            @RequestParam String openKey) {
        return riskDecisionService.riskIdentification(data, openKey);
    }

    /**
     * 账号安全检测
     */
    @PostMapping("/account-security")
    public Response<RiskDecisionResult> accountSecurity(@RequestParam String data,
                                                         @RequestParam String openKey) {
        return riskDecisionService.accountSecurity(data, openKey);
    }
}
