package com.soda.risk.engine.config.feature;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础信息特征实体
 */
@Data
@TableName("t_base_info_feature")
public class BaseInfoFeature {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String featureKey;
    private String featureType;
    private String dataType;
    private String sceneKey;
    private Integer state;
    private String description;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
