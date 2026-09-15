package com.segi.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.segi.parking.entity.ParkingSpace;
import java.util.List;

public interface ParkingSpaceService extends IService<ParkingSpace> {
    // 获取当前校园内所有车位的实时状态列表
    List<ParkingSpace> getAllSpacesStatus();
}