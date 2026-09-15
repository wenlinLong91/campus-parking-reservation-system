package com.segi.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("parking_reservation") // 映射你 MySQL 中的预约表
public class ParkingReservation {

    @TableId(value = "id", type = IdType.AUTO) // 主键自增
    private Long id;

    private Long userId;       // 关联用户 id (sys_user)

    private Long spaceId;      // 关联车位 id (parking_space)

    private String carNumber;  // 预约时填写的车牌号

    private LocalDateTime startTime; // 预约开始时间

    private LocalDateTime endTime;   // 预约结束时间

    private String resStatus;  // 状态：RESERVED（已预约）/ USED（已履约）/ CANCELLED（已取消）/ TIMEOUT（超时）
}