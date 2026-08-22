package com.soda.risk.engine.config.disposer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处置信息实体 - 记录用户被处置的信息
 */
@Data
@TableName("t_disposer_info")
public class DisposerInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;
    private String disposerType;
    private String disposerKey;
    private String strategyKey;
    private String sceneKey;
    private Integer state;
    private String description;
    private String operator;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
