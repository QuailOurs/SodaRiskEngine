package com.soda.risk.engine.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soda.risk.engine.config.riskdecision.BlackWhiteList;
import com.soda.risk.engine.config.riskdecision.BlackWhiteListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 黑白名单管理Controller - 匹配前端 risk-decision-config-center/blackWhiteList/* 路径
 */
@RestController
@RequestMapping("/api/risk-decision-config-center/blackWhiteList")
@RequiredArgsConstructor
public class BlackWhiteListController {

    private final BlackWhiteListService blackWhiteListService;

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<BlackWhiteList> query = new LambdaQueryWrapper<>();
        if (reqData != null) {
            Object listType = reqData.get("listType");
            Object listKey = reqData.get("listKey");
            Object listValue = reqData.get("listValue");
            if (listType != null && !listType.toString().isBlank()) query.eq(BlackWhiteList::getListType, listType.toString());
            if (listKey != null && !listKey.toString().isBlank()) query.eq(BlackWhiteList::getListKey, listKey.toString());
            if (listValue != null && !listValue.toString().isBlank()) query.like(BlackWhiteList::getListValue, listValue.toString());
        }
        result.put("code", 200);
        result.put("data", blackWhiteListService.list(query.orderByAsc(BlackWhiteList::getId)));
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", blackWhiteListService.getById(id));
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody BlackWhiteList item) {
        Map<String, Object> result = new HashMap<>();
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        if (item.getState() == null) item.setState(1);
        result.put("code", 200);
        result.put("data", blackWhiteListService.save(item));
        blackWhiteListService.syncToRedis();
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody BlackWhiteList item) {
        Map<String, Object> result = new HashMap<>();
        item.setUpdateTime(LocalDateTime.now());
        result.put("code", 200);
        result.put("data", blackWhiteListService.updateById(item));
        blackWhiteListService.syncToRedis();
        return result;
    }

    @PostMapping("/update/state")
    public Map<String, Object> updateState(@RequestBody Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        Long id = Long.valueOf(reqData.get("id").toString());
        Integer state = Integer.valueOf(reqData.get("state").toString());
        BlackWhiteList item = blackWhiteListService.getById(id);
        if (item != null) {
            item.setState(state);
            item.setUpdateTime(LocalDateTime.now());
            blackWhiteListService.updateById(item);
            blackWhiteListService.syncToRedis();
            result.put("code", 200);
            result.put("data", true);
        } else {
            result.put("code", 404);
            result.put("msg", "记录不存在");
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", blackWhiteListService.removeById(id));
        blackWhiteListService.syncToRedis();
        return result;
    }

    @PostMapping("/validExist/config")
    public Map<String, Object> validExist(@RequestBody Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        Object listType = reqData.get("listType");
        Object listKey = reqData.get("listKey");
        Object listValue = reqData.get("listValue");
        result.put("data", blackWhiteListService.count(new LambdaQueryWrapper<BlackWhiteList>()
                .eq(listType != null, BlackWhiteList::getListType, listType)
                .eq(listKey != null, BlackWhiteList::getListKey, listKey)
                .eq(listValue != null, BlackWhiteList::getListValue, listValue)) > 0);
        return result;
    }
}
