package com.soda.risk.engine.service.impl;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.dto.RiskDecisionResult;
import com.soda.risk.engine.api.interfaces.IRiskDecisionService;
import com.soda.risk.engine.common.enums.CodeEnum;
import com.soda.risk.engine.core.riskdecision.engine.RiskDecisionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 风险决策服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskDecisionServiceImpl implements IRiskDecisionService {

    private final RiskDecisionEngine riskDecisionEngine;

    @Override
    public Response<RiskDecisionResult> riskIdentification(String data, String openKey) {
        return riskDecisionEngine.execute(data, openKey, "riskIdentification");
    }

    @Override
    public Response<RiskDecisionResult> accountSecurity(String data, String openKey) {
        return riskDecisionEngine.execute(data, openKey, "accountSecurity");
    }
}
