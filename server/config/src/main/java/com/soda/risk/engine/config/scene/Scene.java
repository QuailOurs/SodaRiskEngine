package com.soda.risk.engine.config.scene;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 场景实体
 */
@Data
@TableName("t_scene")
public class Scene {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String sceneKey;
    private Long businessSideId;
    private String businessSideKey;
    @TableField(exist = false)
    private String businessSideName;
    private String pmAccount;
    private String rdAccount;
    private Integer state;
    private String description;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
