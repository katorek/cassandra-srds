package com.wjaronski.cassandrademo.controller

import com.wjaronski.cassandrademo.model.dto.ReservationDatesDto
import com.wjaronski.cassandrademo.service.FlowService
import com.wjaronski.cassandrademo.service.StressTestService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Created by Wojciech Jaronski
 *
 */
@RestController
@RequestMapping("/api/v1/test")
class TestController(
        private val service: FlowService,
        private val stressService: StressTestService
) {

    @PostMapping("/full_reservation")
    @ApiOperation("Reserve room if possible")
    @ApiResponse(code = 200, message = "Status message with additional information")
    fun fullReservation(
            @RequestBody reservationInfo: ReservationDatesDto
    ): Any {
        return service.doReservation(reservationInfo)
    }

    @GetMapping("/truncateTables")
    fun truncateTables(
    ) {
        service.truncateDataTables()
    }

    @PostMapping("/test/{requests}")
    fun stressTest(
            @RequestParam(defaultValue = "100") requests: Int,
            @RequestParam("dateStart", defaultValue = "2020-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateStart: Date,
            @RequestParam("endDate", defaultValue = "2020-12-31") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: Date
    ): String {
        return stressService.testX(load = requests, dateRange = Pair(dateStart, endDate))
    }

}