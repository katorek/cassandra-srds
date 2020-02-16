package com.wjaronski.cassandrademo.controller

import com.wjaronski.cassandrademo.model.dto.ReservationDatesDto
import com.wjaronski.cassandrademo.model.dto.RoomAvailabilityDto
import com.wjaronski.cassandrademo.service.ReservationService
import io.swagger.annotations.ApiOperation
import org.springframework.context.annotation.Profile
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * Created by Wojciech Jaronski
 *
 */

@RestController
@RequestMapping("/api/v1/rooms")
@Profile("devRestTest")
class RoomController(
        val service: ReservationService
) {


    @GetMapping
    fun getRooms(@RequestParam("year", required = true) year: Int,
                 @RequestParam("month") month: Int,
                 @RequestParam("roomSize") roomSize: Int): Optional<Set<Int>> {

        return service.getRoomAvailability(RoomAvailabilityDto(
                year = year,
                month = month,
                howManyPeople = roomSize
        ))
    }


    @GetMapping("/counter")
    @ApiOperation("Get number of ")
    fun getCounter(
            @RequestParam("dateStart", defaultValue = "2020-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateStart: Date,
            @RequestParam("endDate", defaultValue = "2020-01-10") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: Date,
            @RequestParam("roomSize", defaultValue = "1") roomSize: Int
    ): Optional<Collection<Long>> {
        return service.getCounter(
                ReservationDatesDto(
                        roomSize = roomSize,
                        startDate = dateStart,
                        endDate = endDate
                ))
    }
}