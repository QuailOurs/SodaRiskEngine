package com.soda.risk.engine.config.feature;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 算法特征实体
 */
@Data
@TableName("t_algorithm_feature")
public class AlgorithmFeature {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String sceneKey;
    private String modelKey;
    private String inputFields;
    private String outputField;
    private String description;
    private Integer state;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
