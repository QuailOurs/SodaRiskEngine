package com.soda.risk.engine.config.controller;

import com.soda.risk.engine.config.disposer.DisposerInfo;
import com.soda.risk.engine.config.disposer.DisposerInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 处置信息管理Controller - 匹配前端 disposer-config-center/disposerInfo/* 路径
 */
@RestController
@RequestMapping("/api/disposer-config-center/disposerInfo")
@RequiredArgsConstructor
public class DisposerInfoController {

    private final DisposerInfoService disposerInfoService;

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", disposerInfoService.list());
        return result;
    }

    @PostMapping("/releasePunish")
    public Map<String, Object> releasePunish(@RequestBody Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        String userId = (String) reqData.get("userId");
        result.put("code", 200);
        result.put("data", disposerInfoService.releasePunish(userId));
        return result;
    }

    @PostMapping("/releasePunishBatch")
    public Map<String, Object> releasePunishBatch(@RequestParam List<String> userIds) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", disposerInfoService.releasePunishBatch(userIds));
        return result;
    }
}
