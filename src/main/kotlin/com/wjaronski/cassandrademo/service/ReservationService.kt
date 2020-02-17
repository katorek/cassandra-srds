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
        private val roomRepository: RoomRepository,
        private val prereservationRepo: PrereservationRepository,
        private val roomReservationRepo: RoomReservationRepository,
        private val reservationRepo: ReservationRepository
) {

    // RoomRepository methods

    fun insertData(data: List<RoomData>) {
        roomRepository.insertData(data)
    }

    /**
     *     returns list of rooms avaialble to reserve
     *
     *     example [101, 102, 103]
     *     example null
     */
    fun getRoomAvailability(dto: RoomAvailabilityDto): Optional<Set<Int>> {
        return roomRepository.getRoomAvailability(dto)
    }

    // PrereservationRepository  methods

    fun incrementCounter(dto: ReservationDatesDto) {
        validateDates(dto)
        prereservationRepo.incrementCounter(dto)
    }

    private fun validateDates(dto: ReservationDatesDto) {
        if (dto.startDate.after(dto.endDate))
            throw RuntimeException("Invalid date range")
    }

    fun decrementCounter(dto: ReservationDatesDto) {
        validateDates(dto)
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

    fun getRoomsReservations(dto: ReservationDatesDto): List<Optional<List<RoomReservationDto>>> {
        validateDates(dto)
        return roomReservationRepo.getRoomsReservations(dto)
    }

    fun appendRoomReservation(dto: RoomReservationDto) {
        val dates = dto.dates
        if (dates != null) {
            validateDates(dates)
        }
        roomReservationRepo.appendRoomReservation(dto)
    }

    fun removeRoomReservation(dto: RoomReservationDto) {
        val dates = dto.dates
        if (dates != null) {
            validateDates(dates)
        }
        roomReservationRepo.removeRoomReservation(dto)
    }

    fun removeReservationInfo(uuid: UUID) {
        reservationRepo.removeReservationInfo(uuid)
    }

    fun truncateDataTables() {
        roomRepository.truncate()
        prereservationRepo.truncate()
        roomReservationRepo.truncate()
        reservationRepo.truncate()
    }
}