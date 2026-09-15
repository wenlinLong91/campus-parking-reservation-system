package com.segi.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.segi.parking.entity.ParkingReservation;
import com.segi.parking.entity.ParkingSpace;
import com.segi.parking.mapper.ParkingReservationMapper;
import com.segi.parking.mapper.ParkingSpaceMapper;
import com.segi.parking.service.ParkingReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParkingReservationServiceImpl extends ServiceImpl<ParkingReservationMapper, ParkingReservation> implements ParkingReservationService {

    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;

    @Override
    @Transactional // 开启事务，保证高并发时预订数据的一致性
    public String makeReservation(ParkingReservation reservation) {
        // 1. 检查同一个车位，在用户选择的时间段内是否已经被别人抢先预约了
        LambdaQueryWrapper<ParkingReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingReservation::getSpaceId, reservation.getSpaceId())
                .eq(ParkingReservation::getResStatus, "RESERVED")
                .lt(ParkingReservation::getStartTime, reservation.getEndTime())  // 业务算法：新开始时间 < 已有结束时间
                .gt(ParkingReservation::getEndTime, reservation.getStartTime()); // 业务算法：新结束时间 > 已有开始时间

        Long conflictCount = baseMapper.selectCount(queryWrapper);
        if (conflictCount > 0) {
            return "The parking space is already occupied during this time slot!";
        }

        // 2. 保存预约订单
        reservation.setResStatus("RESERVED");
        baseMapper.insert(reservation);


        // 3. 将对应的车位状态同步修改为占用状态 (1 代表 OCCUPIED)
        ParkingSpace space = parkingSpaceMapper.selectById(reservation.getSpaceId());
        if (space != null) {
            space.setStatus(1); // 这里的 setSpaceStatus 改为 setStatus，传入数字 1
            parkingSpaceMapper.updateById(space);
        }

        return "Reservation success";
    }

    @Override
    public List<ParkingReservation> getUserReservations(Long userId) {
        LambdaQueryWrapper<ParkingReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingReservation::getUserId, userId)
                .orderByDesc(ParkingReservation::getStartTime); // 按照预订时间从新到旧排序
        return baseMapper.selectList(queryWrapper);
    }
}
