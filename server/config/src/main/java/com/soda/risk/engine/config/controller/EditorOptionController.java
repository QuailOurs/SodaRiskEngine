package com.soda.risk.engine.config.controller;

import com.soda.risk.engine.config.catalog.ConfigurationCatalogSupport;
import com.soda.risk.engine.config.catalog.ConfigurationCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** 策略与规则编辑器的选项查询和表达式校验接口。 */
@RestController
@RequestMapping("/api/strategy-engine-config-center")
@RequiredArgsConstructor
public class EditorOptionController {

    private final ConfigurationCatalogSupport support;
    private final ConfigurationCatalogService catalogService;

    @PostMapping("/tool/list")
    public Map<String, Object> toolList(@RequestBody(required = false) Map<String, Object> request) {
        Integer type = request == null || request.get("type") == null || request.get("type").toString().isBlank()
                ? null : Integer.valueOf(request.get("type").toString());
        List<Map<String, Object>> rows = support.listTools(type).stream().filter(row -> {
            String name = request == null ? "" : Objects.toString(request.get("name"), "");
            String state = request == null ? "" : Objects.toString(request.get("state"), "");
            return (name.isBlank() || Objects.toString(row.get("name"), "").contains(name)) &&
                    (state.isBlank() || Objects.equals(row.get("state"), state));
        }).toList();
        return response(page(rows, request));
    }

    @GetMapping("/tool/getToolFieldCascaderDataGroupByTool/toolType/{toolId}/multiple/{multiple}")
    public Map<String, Object> toolFields(@PathVariable long toolId, @PathVariable boolean multiple) {
        return response(support.toolFields(toolId, multiple));
    }

    @PostMapping("/parameter/list")
    public Map<String, Object> parameterList(@RequestBody(required = false) Map<String, Object> request) {
        return response(catalogService.parameterList(request == null ? Map.of() : request));
    }

    @GetMapping("/parameter/list/{sceneKey}")
    public Map<String, Object> parameterList(@PathVariable String sceneKey) {
        return response(catalogService.parameterListByScene(sceneKey));
    }

    @GetMapping("/parameter/getCascaderDataGroupParamIdBySceneKey/sceneKey/{sceneKey}/multiple/{multiple}")
    public Map<String, Object> parameterIds(@PathVariable String sceneKey, @PathVariable boolean multiple) {
        return response(support.parameterCascader(sceneKey, false, multiple));
    }

    @GetMapping("/parameter/getCascaderDataGroupParamKeyBySceneKey/sceneKey/{sceneKey}/multiple/{multiple}")
    public Map<String, Object> parameterKeys(@PathVariable String sceneKey, @PathVariable boolean multiple) {
        return response(support.parameterCascader(sceneKey, true, multiple));
    }

    @PostMapping("/checkExpression/checkRegExp")
    public Map<String, Object> checkExpression(@RequestBody Map<String, Object> request) {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            String input = String.valueOf(request.getOrDefault("testData", ""));
            String field = String.valueOf(request.getOrDefault("searchField", ""));
            String regex = String.valueOf(request.getOrDefault("regExp", ""));
            data.put("matched", Pattern.compile(regex).matcher(input).find());
            data.put("searchField", field);
            data.put("valid", true);
        } catch (RuntimeException error) {
            data.put("matched", false);
            data.put("valid", false);
            data.put("message", error.getMessage());
        }
        return response(data);
    }

    @PatchMapping("/tool/enable/{id}")
    public Map<String, Object> enableTool(@PathVariable long id) {
        catalogService.updateToolState(id, 1); return response(true);
    }

    @PatchMapping("/tool/forbidden/{id}")
    public Map<String, Object> disableTool(@PathVariable long id) {
        catalogService.updateToolState(id, 0); return response(true);
    }

    private Object page(List<Map<String, Object>> rows, Map<String, Object> request) {
        if (request == null || request.get("currentPage") == null || request.get("pageSize") == null) return rows;
        int current = Math.max(1, Integer.parseInt(request.get("currentPage").toString()));
        int size = Math.max(1, Integer.parseInt(request.get("pageSize").toString()));
        int from = Math.min(rows.size(), (current - 1) * size); int to = Math.min(rows.size(), from + size);
        Map<String, Object> page = new LinkedHashMap<>(); page.put("records", rows.subList(from, to));
        page.put("current", current); page.put("size", size); page.put("total", rows.size());
        page.put("pages", (rows.size() + size - 1) / size); return page;
    }

    private Map<String, Object> response(Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }
}
