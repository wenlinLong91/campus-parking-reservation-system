package com.segi.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.segi.parking.entity.ParkingReservation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingReservationMapper extends BaseMapper<ParkingReservation> {
}