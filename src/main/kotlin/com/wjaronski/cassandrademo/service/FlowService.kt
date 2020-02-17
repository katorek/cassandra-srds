package com.wjaronski.cassandrademo.service

import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.ProgressStatus
import com.wjaronski.cassandrademo.model.dto.ReservationDatesDto
import com.wjaronski.cassandrademo.model.dto.ReservationInfoDto
import com.wjaronski.cassandrademo.model.dto.RoomAvailabilityDto
import com.wjaronski.cassandrademo.model.dto.RoomReservationDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

/**
 * Created by Wojciech Jaronski
 *
 */
@Service
class FlowService(
        private val service: ReservationService
) {
    private val logger by LoggerDelegate()

    private fun sleep(milis: Long) {
        runBlocking {
            delay(TimeUnit.MILLISECONDS.toMillis(milis))
        }
    }

    fun doReservation(request: ReservationDatesDto): Any {

        // measure time
        val startTime = System.currentTimeMillis()

        // setting variables
        val roomSize = request.roomSize
        val c1 = Calendar.getInstance()
        val c2 = Calendar.getInstance()
        c1.time = request.startDate
        c2.time = request.endDate

        val year = c1.get(Calendar.YEAR)
        val monthStart = c1.get(Calendar.MONTH)
        val monthEnd = c2.get(Calendar.MONTH)

        val weekStart = c1.get(Calendar.WEEK_OF_YEAR)
        val weekEnd = c2.get(Calendar.WEEK_OF_YEAR)

        val dayStart = c1.get(Calendar.DAY_OF_YEAR)
        val dayEnd = c2.get(Calendar.DAY_OF_YEAR)


        // stage 1
        // increment counters
        service.incrementCounter(request)
        sleep(100)

        // todo reservation across multiple months
        val availability = service.getRoomAvailability(RoomAvailabilityDto(year = year, month = monthStart, howManyPeople = roomSize))

        val counters = service.getCounter(request)

        if (!areFreeRoomsInPeriod(availability, counters)) {
            // free (decreaces counters) taken room spaces
            service.decrementCounter(request)
            return "No room available at given date range"
        }

        // stage 2     CHECK IF ANY ROOM IS AVAILABLE ACROSS WHOLE RESERVATION
        var takenRooms = service.getRoomsReservations(request)
        var freeRooms = calculateFreeRooms(allRooms = availability, takenRooms = takenRooms)

        if (freeRooms.isEmpty()) {
            service.decrementCounter(request)
            return "There are no rooms available between given dates"
        }

        // stage 3    LETS RESERVE RANDOM FREE ROOM
        val roomReservationDto = RoomReservationDto.randomUUID(freeRooms.random(), request)
        val uuid = roomReservationDto.reservation

        service.appendRoomReservation(roomReservationDto)
        service.insertReservation(ReservationInfoDto.withUUID(uuid, roomReservationDto.toString()))

        sleep(300)
        val endTime = System.currentTimeMillis() - startTime
        logger.debug("Elapsed time {} ms, {} s", TimeUnit.MILLISECONDS.toMillis(endTime), TimeUnit.MILLISECONDS.toSeconds(endTime))
        logger.debug("Will wait 3x {} ms", endTime)
        takenRooms = service.getRoomsReservations(roomReservationDto.dates!!)

        val retries = 5
        var status = ProgressStatus.RETRYING
        for (i in 0..retries) {
            if (doesAvailabilityContainsOtherReservationsOnMyRoom(takenRooms, roomReservationDto)) {
                status = ProgressStatus.FAILURE
                //retry
                service.removeRoomReservation(roomReservationDto)
                sleep(2 * endTime + ThreadLocalRandom.current().nextLong(endTime))
                takenRooms = service.getRoomsReservations(request)
                freeRooms = calculateFreeRooms(allRooms = availability, takenRooms = takenRooms)

                if (freeRooms.isEmpty()) {
                    service.decrementCounter(request)
                    return "There are no rooms available between given dates"
                }

                roomReservationDto.room = freeRooms.random()
                service.appendRoomReservation(roomReservationDto)
                service.insertReservation(ReservationInfoDto.withUUID(uuid, roomReservationDto.toString()))
            } else {
                status = ProgressStatus.SUCCESS
                break
            }
        }

        when (status) {
            ProgressStatus.FAILURE -> {
                logger.info("Failed to book any room after {} retries. Cleaning", retries)
                service.removeRoomReservation(roomReservationDto)
                service.decrementCounter(request)
                service.removeReservationInfo(uuid)
                //todo remove uuid from reservations
                return "Failed to book any room after $retries retries"
            }
            ProgressStatus.SUCCESS -> {
                return "Successfully booked reservation. $roomReservationDto"
            }
            else -> {
                logger.info("Unknown state '{}'", status)
                return "Unknown state. Contact support. Provide UUID: '$uuid'"
            }
        }

    }


    private fun doesAvailabilityContainsOtherReservationsOnMyRoom(reservations: List<Optional<List<RoomReservationDto>>>, roomReservationDto: RoomReservationDto): Boolean {
        return reservations.map { it.orElse(emptyList()) }.any { containsDuplicatesOnRoom(roomReservationDto.room, it) }
    }

    private fun containsDuplicatesOnRoom(room: Int, list: List<RoomReservationDto>?): Boolean {
        if (list != null) {
            return list.map { it.room }.filter { it.compareTo(room) == 0 }.count() > 1
        }
        return false
    }

    /**
     *    Example:
     *     allRooms - [101, 102, 103, 104] (month)
     *     takenRooms:
     *     [
     *       [(101, R1), (103, R2), (104, R3)]
     *       [(101, R1), (104, R3)]
     *       []
     *     ]
     *
     *     result [102]
     */
    private fun calculateFreeRooms(allRooms: Optional<Set<Int>>, takenRooms: List<Optional<List<RoomReservationDto>>>): Set<Int> {
        val distinct = distinct(takenRooms)
        if (allRooms.isPresent) {
            return allRooms.get().subtract(distinct).toSet()
        }
        return emptySet()
    }

    private fun distinct(rooms: List<Optional<List<RoomReservationDto>>>): Set<Int> {
        return rooms.flatMap { day -> if (day.isPresent) day.get().map { it.room } else emptySet<Int>() }.distinct().toSet()
    }

    private fun areFreeRoomsInPeriod(availability: Optional<Set<Int>>, counters: Optional<Collection<Long>>): Boolean {
        if (availability.isPresent) {
            val numberOfRooms = availability.get().size
            // cnt 10 20 30
            // ava    20
            // cmp -1 0  1
            return counters.get().all { it.toInt().compareTo(numberOfRooms) <= 0 }
        }
        return false
    }

}