package com.soda.risk.engine.config.riskdecision;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 返回码实体
 */
@Data
@TableName("t_return_code")
public class ReturnCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String returnCode;
    private String name;
    private String description;
    private String sceneKey;
    private Integer state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
