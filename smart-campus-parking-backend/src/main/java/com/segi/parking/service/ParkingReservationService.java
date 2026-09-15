package com.segi.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.segi.parking.entity.ParkingReservation;
import java.util.List;

public interface ParkingReservationService extends IService<ParkingReservation> {
    // 核心智能预约业务逻辑：处理冲突校验并锁定制定的时段
    String makeReservation(ParkingReservation reservation);

    // 根据用户 ID 查询个人的历史预约记录列表
    List<ParkingReservation> getUserReservations(Long userId);
}