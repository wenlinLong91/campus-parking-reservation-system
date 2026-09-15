package com.segi.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.segi.parking.entity.User;
import com.segi.parking.mapper.UserMapper;
import com.segi.parking.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 引入密码加密工具（符合文档要求的 Password encryption storage）
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String register(User user) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User existingUser = baseMapper.selectOne(queryWrapper);

        if (existingUser != null) {
            return "Username already exists!";
        }

        // 2. 对明文密码进行 BCrypt 强哈希加密
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 3. 设置默认角色和创建时间
        if (user.getRole() == null) {
            user.setRole("STAFF"); // 默认是职工
        }
        user.setCreateTime(LocalDateTime.now());

        // 4. 存入数据库
        baseMapper.insert(user);
        return "Register success";
    }

    @Override
    // 请找到类似下面这段的逻辑，确保它看起来像这样：
    public User login(String username, String password) {
        // 1. 先查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = baseMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 密码校验 (确保这里与你的数据库存储方式匹配，如果是明文就直接 equals)
        if (!passwordEncoder.matches(password, user.getPassword())) {
    throw new RuntimeException("Invalid username or password.");
}

        // 3. 核心：权限校验 (这里是重点！)
        String role = user.getRole();
        if (role == null || (!"ADMIN".equals(role.toUpperCase()) && !"STAFF".equals(role.toUpperCase()))) {
            // 如果这里出问题，前端会收到 500 错误，从而报错 "Server connection error"
            throw new RuntimeException("权限不足：仅允许教职工登录");
        }

        return user;
    }
}
