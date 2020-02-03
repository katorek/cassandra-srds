package com.wjaronski.cassandrademo.conf

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Created by Wojciech Jaronski
 *
 */

@Configuration
class ReservationConfiguration {

    @Value("\${reservation.startDate:2020-01-01}")
    protected var reservationStartDate: String = "2020-01-01"

    @Value("\${reservation.endDate:2020-02-01}")
    protected var reservationEndDate: String = "2020-02-01"

    final var dateFormatterPattern = "yyyy-MM-dd"
    var formatter = DateTimeFormatter.ofPattern(dateFormatterPattern)

    fun reservationStartLocalDate(): LocalDate {
        return LocalDate.parse(reservationStartDate, formatter)
    }

    fun reservationEndLocalDate(): LocalDate {
        return LocalDate.parse(reservationEndDate, formatter)
    }

}