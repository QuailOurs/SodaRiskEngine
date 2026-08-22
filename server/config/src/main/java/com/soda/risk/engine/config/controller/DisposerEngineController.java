package com.soda.risk.engine.config.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 处置引擎Controller - 匹配前端 disposer-engine/* 路径
 */
@RestController
@RequestMapping("/api/disposer-engine")
public class DisposerEngineController {

    @PostMapping("/offlineLock/upLoad")
    public Map<String, Object> offlineLockUpload(@RequestParam(required = false) Object file) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", "上传成功");
        return result;
    }

    @PostMapping("/releasePunish/unBlockHRG")
    public Map<String, Object> unBlockHRG(@RequestBody Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", true);
        return result;
    }

    @PostMapping("/releasePunish/batchUnBlockHRG")
    public Map<String, Object> batchUnBlockHRG(@RequestParam(required = false) Object file) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", true);
        return result;
    }
}
