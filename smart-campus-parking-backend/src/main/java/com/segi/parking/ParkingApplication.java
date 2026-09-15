package com.segi.parking;

import org.mybatis.spring.annotation.MapperScan; // 确保引入了这行
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.segi.parking.mapper") // 🌟 核心：告诉系统去哪里扫描持久层接口
public class ParkingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkingApplication.class, args);
        System.out.println("====== Smart Campus Parking Reservation System backend started successfully. ======");
    }
}