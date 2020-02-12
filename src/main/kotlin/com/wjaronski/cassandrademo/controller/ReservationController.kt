package com.wjaronski.cassandrademo.controller

import com.wjaronski.cassandrademo.service.ReservationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
        var reservationService: ReservationService
) {

    @GetMapping
    fun helloWorld(): String {
        return "Hello World !"
    }
}
