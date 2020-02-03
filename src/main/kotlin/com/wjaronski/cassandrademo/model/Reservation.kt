package com.wjaronski.cassandrademo.model

import java.io.Serializable
import java.time.LocalDate

/**
 * Created by Wojciech Jaronski
 *
 */

class Reservation(
        var roomNumber: String,
        var guestInfo: String,
        var confirmationNumber: String? = "",
        var startDate: LocalDate,
        var endDate: LocalDate
) : Serializable {

    companion object {
        fun fromRequest(request: ReservationRequest): Reservation {
            return Reservation(
                    roomNumber = request.roomNumber,
                    guestInfo = request.guestInfo,
                    startDate = request.startDate,
                    endDate = request.endDate
            )
        }
    }

}