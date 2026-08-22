package com.soda.risk.engine.config.riskdecision;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 黑白名单实体
 */
@Data
@TableName("t_black_white_list")
public class BlackWhiteList {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String listType;
    private String listKey;
    private String listValue;
    private Integer state;
    private String description;
    private String operator;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
