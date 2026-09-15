package com.segi.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.segi.parking.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 后，MyBatis-Plus 会自动帮你生成根据学号、ID 查询和增删改查的所有基础方法，无需手写 SQL！
}