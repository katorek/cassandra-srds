package com.wjaronski.cassandrademo.controller

import com.wjaronski.cassandrademo.model.dto.ReservationDatesDto
import com.wjaronski.cassandrademo.service.FlowService
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by Wojciech Jaronski
 *
 */
@RestController
@RequestMapping("/api/v1/test")
class TestController(
        private val service: FlowService
) {

    @PostMapping("/full_reservation")
    @ApiOperation("Get number of ")
    fun fullReservation(
            @RequestBody reservationInfo: ReservationDatesDto
    ): Any {
        return service.doReservation(reservationInfo)
    }
}