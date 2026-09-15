package com.segi.parking.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("parking_space")
public class ParkingSpace {
    private Long id;

    private Long zoneId; // 对应数据库里的 zone_id

    private String spaceNumber; // 对应数据库里的 space_number

    @TableField("status") // 关键点：明确告诉系统，Java 里的 status 对应数据库里的 status 字段
    private Integer status;
}