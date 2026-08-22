package com.soda.risk.engine.service.auth;

import com.soda.risk.engine.common.cache.RedisCacheService;
import com.soda.risk.engine.common.constants.RedisKeyConstants;
import com.soda.risk.engine.common.enums.CodeEnum;
import com.soda.risk.engine.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 * 通过openKey认证业务方，获取openId
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RedisCacheService redisCacheService;

    /**
     * 认证openKey，返回openId（业务方标识）
     * openKey格式: openId 或 openKey（通过Redis映射）
     *
     * @param openKey 业务方授权码
     * @return openId 业务方标识
     */
    public String auth(String openKey) {
        if (StringUtils.isBlank(openKey)) {
            throw new BizException(CodeEnum.AUTH_FAILED);
        }

        // openKey包含'_'说明为规则引擎场景Key，无需校验
        if (openKey.contains("_")) {
            return openKey;
        }

        String openId = findOpenId(openKey);
        if (openId == null) {
            log.error("openKey not certified: {}", openKey);
            throw new BizException(CodeEnum.AUTH_INVALID_KEY);
        }
        return openId;
    }

    /**
     * 查找openKey对应的openId
     */
    public String findOpenId(String openKey) {
        // 从Redis获取openKey到openId的映射
        String openId = redisCacheService.get(RedisKeyConstants.BUSINESS_SIDE_KEY + openKey);
        if (openId != null) {
            return openId;
        }
        // 如果Redis中没有映射，尝试直接使用openKey作为openId
        Object businessSide = redisCacheService.hGetAll(RedisKeyConstants.BUSINESS_SIDE_OPENID + openKey);
        if (businessSide != null) {
            return openKey;
        }
        return null;
    }
}
