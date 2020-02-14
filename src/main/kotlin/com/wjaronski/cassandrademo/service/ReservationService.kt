package com.wjaronski.cassandrademo.service

import com.wjaronski.cassandrademo.conf.ReservationConfiguration
import com.wjaronski.cassandrademo.model.dto.RoomAvailabilityDto
import com.wjaronski.cassandrademo.repository.ReservationRepository
import com.wjaronski.cassandrademo.repository.RoomRepository
import org.springframework.stereotype.Service
import java.util.*

/**
 * Created by Wojciech Jaronski
 *
 */
@Service
class ReservationService(
        val roomRepository: RoomRepository,
        val reservationRepository: ReservationRepository,
        val reservationConfiguration: ReservationConfiguration
) {


    fun availableRooms(dto: RoomAvailabilityDto): Optional<Set<Int>> {
        return roomRepository.getRoomAvailability(dto)
    }

//    fun insertRoomInfo(dto:)
}