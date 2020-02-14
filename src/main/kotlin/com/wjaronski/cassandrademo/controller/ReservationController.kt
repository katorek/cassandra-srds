package com.wjaronski.cassandrademo.controller

import com.wjaronski.cassandrademo.model.dto.RoomAvailabilityDto
import com.wjaronski.cassandrademo.service.ReservationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
        var reservationService: ReservationService
) {

    //    @GetMapping
    fun helloWorld(): String {
        return "Hello World !"
    }

    @GetMapping
    fun getRooms(@RequestParam("year", required = true) year: Int,
                 @RequestParam("month") month: Int,
                 @RequestParam("roomSize") roomSize: Int): Optional<Set<Int>> {

        return reservationService.availableRooms(RoomAvailabilityDto(
                year = year,
                month = month,
                howManyPeople = roomSize
        ))
    }
}
