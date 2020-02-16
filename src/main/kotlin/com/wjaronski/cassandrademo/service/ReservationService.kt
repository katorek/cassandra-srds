package com.wjaronski.cassandrademo.service

import com.wjaronski.cassandrademo.model.dto.*
import com.wjaronski.cassandrademo.repository.PrereservationRepository
import com.wjaronski.cassandrademo.repository.ReservationRepository
import com.wjaronski.cassandrademo.repository.RoomRepository
import com.wjaronski.cassandrademo.repository.RoomReservationRepository
import org.springframework.stereotype.Service
import java.util.*

/**
 * Created by Wojciech Jaronski
 *
 */
@Service
class ReservationService(
        val roomRepository: RoomRepository,
        val prereservationRepo: PrereservationRepository,
        val roomReservationRepo: RoomReservationRepository,
        val reservationRepo: ReservationRepository
) {

    // RoomRepository methods

    fun insertData(data: List<RoomData>) {
        roomRepository.insertData(data)
    }

    fun getRoomAvailability(dto: RoomAvailabilityDto): Optional<Set<Int>> {
        return roomRepository.getRoomAvailability(dto)
    }

    // PrereservationRepository  methods

    fun incrementCounter(dto: ReservationDatesDto) {
        prereservationRepo.incrementCounter(dto)
    }

    fun decrementCounter(dto: ReservationDatesDto) {
        prereservationRepo.decrementCounter(dto)
    }

    fun getCounter(dto: ReservationDatesDto): Optional<Collection<Long>> {
        return prereservationRepo.getCounter(dto)
    }


    // ReservationRepository methods

    fun getReservationInfo(uuid: UUID): Optional<String> {
        return reservationRepo.getReservation(uuid)
    }

    fun insertReservation(infoDto: ReservationInfoDto): UUID {
        return reservationRepo.insertReservation(infoDto)
    }


    // RoomReservationRepository methods

    fun getRoomsReservations(dto: ReservationDatesDto): Any {
        return roomReservationRepo.getRoomsReservations(dto)
    }

    fun appendRoomReservation(dto: RoomReservationDto) {
        roomReservationRepo.appendRoomReservation(dto)
    }
}