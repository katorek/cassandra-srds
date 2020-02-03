package com.wjaronski.cassandrademo.service

import com.wjaronski.cassandrademo.conf.ReservationConfiguration
import com.wjaronski.cassandrademo.repository.ReservationRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Created by Wojciech Jaronski
 *
 */
@Service
class ReservationService(
        var reservationRepository: ReservationRepository,
        var reservationConfiguration: ReservationConfiguration
) {


    fun availableRooms(sDate: LocalDate, eDate: LocalDate) {

    }
}