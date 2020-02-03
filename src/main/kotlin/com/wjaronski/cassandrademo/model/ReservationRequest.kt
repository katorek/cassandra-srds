package com.wjaronski.cassandrademo.model

import java.time.LocalDate

/**
 * Created by Wojciech Jaronski
 *
 */

class ReservationRequest(
        var roomNumber: String,
        var guestInfo: String,
        var confirmationNumber: String,
        var startDate: LocalDate,
        var endDate: LocalDate
) {
}