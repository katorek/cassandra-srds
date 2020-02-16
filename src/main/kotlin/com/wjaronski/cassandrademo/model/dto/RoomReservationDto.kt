package com.wjaronski.cassandrademo.model.dto

import java.util.*

/**
 * Created by Wojciech Jaronski
 *
 */

data class RoomReservationDto(
        val room: Int,
        val reservation: UUID,
        var dates: ReservationDatesDto? = null
)