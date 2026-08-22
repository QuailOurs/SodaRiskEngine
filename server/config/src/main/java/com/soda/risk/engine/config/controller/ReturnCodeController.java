package com.soda.risk.engine.config.controller;

import com.soda.risk.engine.config.riskdecision.ReturnCode;
import com.soda.risk.engine.config.riskdecision.ReturnCodeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soda.risk.engine.config.scene.Scene;
import com.soda.risk.engine.config.scene.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 返回码管理Controller - 匹配前端 risk-decision-config-center/returnCode/* 路径
 */
@RestController
@RequestMapping("/api/risk-decision-config-center/returnCode")
@RequiredArgsConstructor
public class ReturnCodeController {

    private final ReturnCodeService returnCodeService;
    private final SceneService sceneService;

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<ReturnCode> query = new LambdaQueryWrapper<>();
        if (reqData != null) {
            Object returnCode = reqData.get("returnCode");
            Object name = reqData.get("name");
            Object sceneKey = reqData.get("sceneKey");
            if (returnCode != null && !returnCode.toString().isBlank()) {
                query.like(ReturnCode::getReturnCode, returnCode.toString());
            }
            if (name != null && !name.toString().isBlank()) query.like(ReturnCode::getName, name.toString());
            if (sceneKey != null && !sceneKey.toString().isBlank()) query.eq(ReturnCode::getSceneKey, sceneKey.toString());
        }
        result.put("code", 200);
        result.put("data", returnCodeService.list(query.orderByAsc(ReturnCode::getId)));
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", returnCodeService.getById(id));
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody ReturnCode returnCode) {
        Map<String, Object> result = new HashMap<>();
        returnCode.setCreateTime(LocalDateTime.now());
        returnCode.setUpdateTime(LocalDateTime.now());
        if (returnCode.getState() == null) returnCode.setState(1);
        result.put("code", 200);
        result.put("data", returnCodeService.save(returnCode));
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody ReturnCode returnCode) {
        Map<String, Object> result = new HashMap<>();
        returnCode.setUpdateTime(LocalDateTime.now());
        result.put("code", 200);
        result.put("data", returnCodeService.updateById(returnCode));
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", returnCodeService.removeById(id));
        return result;
    }

    @GetMapping("/allScene")
    public Map<String, Object> allScene() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        List<Map<String, Object>> sceneList = new ArrayList<>();
        for (Scene scene : sceneService.list()) {
            Map<String, Object> item = new HashMap<>();
            item.put("sceneKey", scene.getSceneKey());
            item.put("sceneName", scene.getName());
            sceneList.add(item);
        }
        result.put("data", sceneList);
        return result;
    }

    @GetMapping("/allReturnCode/{sceneKey}")
    public Map<String, Object> allReturnCode(@PathVariable String sceneKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", returnCodeService.getAllReturnCodeBySceneKey(sceneKey));
        return result;
    }

    @GetMapping("/validExist/returnCode/{returnCode}")
    public Map<String, Object> validReturnCodeExist(@PathVariable String returnCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", returnCodeService.validReturnCodeExist(returnCode));
        return result;
    }
}
