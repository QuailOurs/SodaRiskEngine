package com.soda.risk.engine.config.disposer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处置方式配置实体
 */
@Data
@TableName("t_disposer_config")
public class DisposerConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String disposerType;
    private String disposerKey;
    private Integer state;
    private String description;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
