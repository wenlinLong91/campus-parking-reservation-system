package com.segi.parking.controller;

import com.segi.parking.entity.User;
import com.segi.parking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5175")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // 这里建议加上 try-catch 来捕获你在 UserServiceImpl 中抛出的异常
        try {
            User user = userService.login(username, password);

            Map<String, Object> response = new HashMap<>();
            if (user != null) {
                response.put("code", 200);
                response.put("username", user.getUsername());
                response.put("role", user.getRole().toUpperCase());
                return response;
            } else {
                // 如果 user 为 null，也要返回错误信息
                response.put("code", 400);
                response.put("message", "登录失败：用户不存在");
                return response;
            }
        } catch (Exception e) {
            // 捕获 UserServiceImpl 中抛出的 "用户不存在" 或 "密码错误" 异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 400);
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }
}