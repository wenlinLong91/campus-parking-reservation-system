package com.segi.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user") // 对应 MySQL 中的用户验证表
public class User {

    @TableId(value = "id", type = IdType.AUTO) // 主键自增
    private Long id;

    private String userNumber; // 🌟 补齐这个关键字段：工号或学号 (MyBatis-Plus 会自动映射为数据库的 user_number)

    private String username;   // 用户显示姓名

    private String password;   // 密码 (后续可通过 BCrypt 加密存储)

    private String role;       // 角色权限：STUDENT / STAFF / ADMIN

    private LocalDateTime createTime; // 创建时间
}