package com.soda.risk.engine.config.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 规则引擎接入业务方。字段来自原业务方表，示例数据已脱敏。 */
@Data
@TableName("t_business_side")
public class BusinessSide {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String businessSideKey;
    private String systemKey;
    private String description;
    private String operator;
    private Integer state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
