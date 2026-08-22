package com.soda.risk.engine.api.interfaces;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.api.dto.RiskDecisionResult;

/**
 * 风险决策服务接口
 */
public interface IRiskDecisionService {

    /**
     * 风险识别
     * @param data 请求数据JSON
     * @param openKey 业务方Key
     * @return 风险决策结果
     */
    Response<RiskDecisionResult> riskIdentification(String data, String openKey);

    /**
     * 账号安全检测
     * @param data 请求数据JSON
     * @param openKey 业务方Key
     * @return 风险决策结果
     */
    Response<RiskDecisionResult> accountSecurity(String data, String openKey);
}
