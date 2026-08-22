package com.soda.risk.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 处置响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisposerResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 处置方式ID */
    private Long disposerId;
    /** 处置方式名称 */
    private String disposerName;
    /** 处置方式类型 */
    private String disposerType;
    /** 处置结果 */
    private String result;
    /** 是否成功 */
    private boolean success;
    /** 处置消息 */
    private String message;
    /** 用户ID */
    private String userId;
}
