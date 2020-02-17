package com.wjaronski.cassandrademo.model.dto

import java.util.*

/**
 * Created by Wojciech Jaronski
 *
 */

data class RoomReservationDto(
        var room: Int,
        val reservation: UUID,
        var dates: ReservationDatesDto? = null
) {
    companion object {
        fun randomUUID(room: Int, dates: ReservationDatesDto?): RoomReservationDto {
            return RoomReservationDto(
                    room = room,
                    reservation = UUID.randomUUID(),
                    dates = dates
            )
        }
    }

    override fun toString(): String {
        return "{\"room\": $room, \"uuid\": $reservation, \"dates:\" ${dates.toString()}}"
    }
}