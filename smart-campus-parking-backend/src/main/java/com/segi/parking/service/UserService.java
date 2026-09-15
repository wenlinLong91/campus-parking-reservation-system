package com.segi.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.segi.parking.entity.User;

public interface UserService extends IService<User> {
    // 定义这两个方法，这样实现类 UserServiceImpl 才有迹可循
    String register(User user);
    User login(String userNumber, String password);
}