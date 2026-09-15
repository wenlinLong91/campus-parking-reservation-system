package com.segi.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.segi.parking.entity.ParkingSpace;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingSpaceMapper extends BaseMapper<ParkingSpace> {
}