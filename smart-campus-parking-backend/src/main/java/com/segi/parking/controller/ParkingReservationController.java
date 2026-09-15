package com.segi.parking.controller;

import com.segi.parking.common.Result;
import com.segi.parking.entity.ParkingReservation;
import com.segi.parking.service.ParkingReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reservation")
@CrossOrigin
public class ParkingReservationController {

    @Autowired
    private ParkingReservationService parkingReservationService;

    /**
     * 提交车位预约
     * URL: http://localhost:8080/api/reservation/book
     */
    @PostMapping("/book")
    public Result<String> bookSpace(@RequestBody ParkingReservation reservation) {
        String msg = parkingReservationService.makeReservation(reservation);
        if ("Reservation success".equals(msg)) {
            return Result.success(msg);
        }
        return Result.error(msg);
    }

    /**
     * 查询个人预订历史
     * URL: http://localhost:8080/api/reservation/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Result<List<ParkingReservation>> getUserHistory(@PathVariable Long userId) {
        return Result.success(parkingReservationService.getUserReservations(userId));
    }
}