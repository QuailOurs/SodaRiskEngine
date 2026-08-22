package com.soda.risk.engine.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 开发环境的演示认证接口。
 *
 * <p>生产环境不注册此控制器，部署方应通过网关、OAuth2/OIDC 或自定义认证服务
 * 提供等价接口。</p>
 */
@Slf4j
@RestController
@Profile("dev")
public class AuthController {

    /**
     * 登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginReq) {
        String userName = loginReq.getOrDefault("userName", "");
        String password = loginReq.getOrDefault("password", "");

        log.info("Login attempt: userName={}", userName);

        Map<String, Object> result = new HashMap<>();
        if ("admin".equals(userName) && "admin".equals(password)) {
            result.put("code", 200);
            result.put("msg", "success");
            result.put("token", "admin-token-" + System.currentTimeMillis());
        } else if ("test".equals(userName) && "test".equals(password)) {
            result.put("code", 200);
            result.put("msg", "success");
            result.put("token", "test-token-" + System.currentTimeMillis());
        } else {
            result.put("code", 401);
            result.put("msg", "用户名或密码错误");
        }
        return result;
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/get_info")
    public Map<String, Object> getUserInfo(@RequestParam(required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("name", "admin");
        data.put("user_id", "1");
        data.put("access", Arrays.asList("admin"));
        data.put("token", token != null ? token : "admin");
        data.put("avatar", "https://avatars0.githubusercontent.com/u/20942571?s=460&v=4");
        result.put("data", data);
        return result;
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return result;
    }

    /**
     * 获取菜单权限
     */
    @GetMapping("/right")
    public Map<String, Object> getRight() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", Collections.emptyList());
        return result;
    }

    /**
     * 消息数量
     */
    @GetMapping("/message/count")
    public Map<String, Object> getMessageCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", 0);
        return result;
    }

    /**
     * 消息列表
     */
    @GetMapping("/message/init")
    public Map<String, Object> getMessageInit() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("unread", Collections.emptyList());
        data.put("readed", Collections.emptyList());
        data.put("trash", Collections.emptyList());
        result.put("data", data);
        return result;
    }

    /**
     * 消息内容
     */
    @GetMapping("/message/content")
    public Map<String, Object> getMessageContent(@RequestParam String msg_id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", "");
        return result;
    }

    /**
     * 标记已读
     */
    @PostMapping("/message/has_read")
    public Map<String, Object> hasRead(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        return result;
    }

    /**
     * 删除已读
     */
    @PostMapping("/message/remove_readed")
    public Map<String, Object> removeReaded(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        return result;
    }

    /**
     * 还原消息
     */
    @PostMapping("/message/restore")
    public Map<String, Object> restoreTrash(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        return result;
    }
}
