package com.wjaronski.cassandrademo.controller


import com.wjaronski.cassandrademo.model.dto.ReservationDatesDto
import com.wjaronski.cassandrademo.model.dto.ReservationInfoDto
import com.wjaronski.cassandrademo.model.dto.RoomReservationDto
import com.wjaronski.cassandrademo.service.ReservationService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import org.springframework.context.annotation.Profile
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/reservations")
@Profile("devRestTest")
class ReservationController(
        var service: ReservationService
) {

    @PostMapping
    @ApiOperation("Insert new reservation")
    @ApiResponse(code = 200, message = "Returns UUID of reservation")
    fun insertReservation(
            @RequestBody description: String
    ): UUID {
        return service.insertReservation(ReservationInfoDto.withoutUUID(description))
    }

    @GetMapping("/{uuid}")
    fun getReservationInfo(@PathVariable uuid: UUID): ReservationInfoDto {
        return ReservationInfoDto.withUUID(
                uuid = uuid,
                description = service.getReservationInfo(uuid).orElse("Reservation $uuid not found")
        )
    }

    @GetMapping("/rooms")
    fun getRoomsReservations(
            @ApiParam(value = "Start date in format e.x. 2011-12-03", required = true) @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: Date,
            @ApiParam(value = "End date in format 2011-12-03", required = true) @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: Date,
            @ApiParam(value = "Room size. How many people should fit in", required = true) @RequestParam("roomSize") roomSize: Int
    ): Any {
        return service.getRoomsReservations(ReservationDatesDto(
                roomSize = roomSize,
                startDate = startDate,
                endDate = endDate
        ))
    }

    @PostMapping("/rooms")
    fun appendRoomReservation(
            @RequestBody dto: RoomReservationDto
    ) {
        service.appendRoomReservation(dto)
    }
}
