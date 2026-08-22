package com.soda.risk.engine.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soda.risk.engine.config.business.BusinessSide;
import com.soda.risk.engine.config.business.BusinessSideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 员工信息/业务方Controller - 匹配前端 strategy-engine-config-center/emp/* 和 businessside/* 路径
 */
@RestController
@RequestMapping("/api/strategy-engine-config-center")
@RequiredArgsConstructor
public class EmpBusinessController {

    private final BusinessSideService businessSideService;

    @GetMapping("/emp/info/account/{oaAccount}")
    public Map<String, Object> getEmpInfo(@PathVariable String oaAccount) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("oaAccount", oaAccount);
        data.put("name", oaAccount);
        data.put("department", "工程研发部");
        result.put("data", data);
        return result;
    }

    @PostMapping({"/businessside/list", "/businessSide/list"})
    public Map<String, Object> businessSideList(@RequestBody(required = false) Map<String, Object> reqData) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        LambdaQueryWrapper<BusinessSide> query = new LambdaQueryWrapper<>();
        if (reqData != null) {
            Object name = reqData.get("name");
            Object businessSideKey = reqData.get("businessSideKey");
            Object systemKey = reqData.get("systemKey");
            if (name != null && !name.toString().isBlank()) query.like(BusinessSide::getName, name.toString());
            if (businessSideKey != null && !businessSideKey.toString().isBlank()) {
                query.like(BusinessSide::getBusinessSideKey, businessSideKey.toString());
            }
            if (systemKey != null && !systemKey.toString().isBlank()) query.eq(BusinessSide::getSystemKey, systemKey.toString());
        }
        List<BusinessSide> rows = businessSideService.list(query.orderByAsc(BusinessSide::getId));
        result.put("data", pageIfRequested(rows, reqData));
        return result;
    }

    @GetMapping({"/businessside/listAll", "/businessSide/listAll"})
    public Map<String, Object> businessSideListAll() {
        return businessSideList(Collections.emptyMap());
    }

    @GetMapping({"/businessside/{id}", "/businessSide/{id}"})
    public Map<String, Object> getById(@PathVariable Long id) {
        return response(businessSideService.getById(id));
    }

    @PostMapping({"/businessside/add", "/businessSide/add"})
    public Map<String, Object> add(@RequestBody BusinessSide businessSide) {
        if (businessSideService.existsByKey(businessSide.getBusinessSideKey(), null)) {
            return error("业务方标识已存在");
        }
        businessSide.setId(null);
        businessSide.setState(businessSide.getState() == null ? 1 : businessSide.getState());
        businessSide.setOperator(defaultOperator(businessSide.getOperator()));
        businessSide.setCreateTime(LocalDateTime.now());
        businessSide.setUpdateTime(LocalDateTime.now());
        businessSideService.save(businessSide);
        return response(businessSide);
    }

    @PutMapping({"/businessside/update", "/businessSide/update"})
    public Map<String, Object> update(@RequestBody BusinessSide businessSide) {
        if (businessSide.getId() == null) return error("缺少业务方ID");
        if (businessSideService.existsByKey(businessSide.getBusinessSideKey(), businessSide.getId())) {
            return error("业务方标识已存在");
        }
        businessSide.setOperator(defaultOperator(businessSide.getOperator()));
        businessSide.setUpdateTime(LocalDateTime.now());
        businessSideService.updateById(businessSide);
        return response(businessSide);
    }

    @DeleteMapping({"/businessside/{id}", "/businessSide/{id}"})
    public Map<String, Object> delete(@PathVariable Long id) {
        return response(businessSideService.removeById(id));
    }

    @GetMapping({"/businessside/existed/name/{name}", "/businessSide/existed/name/{name}"})
    public Map<String, Object> existedName(@PathVariable String name,
                                           @RequestParam(required = false) Long excludeId) {
        return response(businessSideService.existsByName(name, excludeId) ? "existed" : "available");
    }

    @GetMapping({"/businessside/existed/key/{key}", "/businessSide/existed/key/{key}"})
    public Map<String, Object> existedKey(@PathVariable String key,
                                          @RequestParam(required = false) Long excludeId) {
        return response(businessSideService.existsByKey(key, excludeId) ? "existed" : "available");
    }

    @GetMapping({"/businessside/systemKey/list", "/businessSide/systemKey/list"})
    public Map<String, Object> systemKeyList() {
        List<String> values = businessSideService.list().stream()
                .map(BusinessSide::getSystemKey)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .distinct()
                .sorted()
                .toList();
        return response(values);
    }

    private Map<String, Object> response(Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 400);
        result.put("msg", message);
        return result;
    }

    private String defaultOperator(String operator) {
        return operator == null || operator.isBlank() ? "admin" : operator;
    }

    private Object pageIfRequested(List<BusinessSide> rows, Map<String, Object> request) {
        if (request == null || request.get("currentPage") == null || request.get("pageSize") == null) return rows;
        int current = Math.max(1, Integer.parseInt(request.get("currentPage").toString()));
        int size = Math.max(1, Integer.parseInt(request.get("pageSize").toString()));
        int from = Math.min(rows.size(), (current - 1) * size);
        int to = Math.min(rows.size(), from + size);
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("records", rows.subList(from, to));
        page.put("current", current);
        page.put("size", size);
        page.put("total", rows.size());
        page.put("pages", (rows.size() + size - 1) / size);
        return page;
    }
}
