package com.segi.parking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.segi.parking.entity.ParkingSpace;
import com.segi.parking.mapper.ParkingSpaceMapper;
import com.segi.parking.service.ParkingSpaceService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ParkingSpaceServiceImpl extends ServiceImpl<ParkingSpaceMapper, ParkingSpace> implements ParkingSpaceService {

    @Override
    public List<ParkingSpace> getAllSpacesStatus() {
        // 直接调用 MyBatis-Plus 的 selectList 查询表中所有数据
        return baseMapper.selectList(null);
    }
}