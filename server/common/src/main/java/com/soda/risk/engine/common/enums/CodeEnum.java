package com.soda.risk.engine.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应码枚举
 */
@Getter
@AllArgsConstructor
public enum CodeEnum {

    SUCCESS(0, "成功"),
    SYSTEM_ERROR(-1, "系统异常"),

    // 参数错误 1xxx
    PARAM_ERROR(1001, "参数错误"),
    PARAM_NULL(1002, "参数为空"),
    PARAM_INVALID(1003, "参数无效"),
    DATA_PARSING_FAILED(1004, "数据解析失败"),
    DATA_EMPTY(1005, "数据为空或不完整"),

    // 认证错误 2xxx
    AUTH_FAILED(2001, "认证失败"),
    AUTH_EXPIRED(2002, "认证过期"),
    AUTH_INVALID_KEY(2003, "无效的Key"),

    // 业务错误 3xxx
    STRATEGY_NOT_FOUND(3001, "策略不存在"),
    RULE_NOT_FOUND(3002, "规则不存在"),
    FEATURE_NOT_FOUND(3003, "特征不存在"),
    SCENE_NOT_FOUND(3004, "场景不存在"),
    DISPOSER_NOT_FOUND(3005, "处置方式不存在"),

    // 引擎错误 4xxx
    ENGINE_COMPUTE_ERROR(4001, "引擎计算异常"),
    ENGINE_TIMEOUT(4002, "引擎超时"),
    ENGINE_CIRCUIT_BREAK(4003, "熔断降级"),

    // 第三方错误 5xxx
    THIRD_PARTY_ERROR(5001, "第三方服务异常"),
    THIRD_PARTY_TIMEOUT(5002, "第三方服务超时"),
    REDIS_ERROR(5003, "Redis操作异常"),
    DB_ERROR(5004, "数据库操作异常"),
    ;

    private final int code;
    private final String msg;
}
