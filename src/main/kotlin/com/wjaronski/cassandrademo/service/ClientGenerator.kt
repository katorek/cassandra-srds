package com.wjaronski.cassandrademo.service

import com.wjaronski.cassandrademo.conf.AppSettings
import com.wjaronski.cassandrademo.model.dto.ReservationDatesDto
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max

/**
 * Created by Wojciech Jaronski
 *
 */
@Component
class ClientGenerator(
        private val appSettings: AppSettings
) {
    private val minTime = 1
    private val maxTime = 7

    private val minSize = appSettings.room.minSize
    private val maxSize = appSettings.room.maxSize

    private val c1 = Calendar.getInstance()
    private val c2 = Calendar.getInstance()

    private val listOfDays = mutableListOf<Date>()

    init {
        generateDates()
    }

    private fun generateDates() {
        c1.set(2020, 0, 1)
        c2.set(2020, 11, 31)
        while (c1.before(c2)) {
            listOfDays.add(c1.time)
            c1.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun getRandomDates(dateRange: Pair<Date, Date>?): Pair<Int, Pair<Date, Date>> {
        var lowerIdx = 0
        var upperIdx = listOfDays.lastIndex
        if (dateRange != null) {
            for ((idx, date) in listOfDays.withIndex()) {
                if (date.before(dateRange.first)) {
                    lowerIdx = idx
                }
            }
            for ((idx, date) in listOfDays.reversed().withIndex()) {
                if (date.after(dateRange.first)) {
                    upperIdx = idx
                }
            }
        }


        val idx = max(ThreadLocalRandom.current().nextInt(upperIdx - lowerIdx - 1 - maxTime) + lowerIdx, 0)
        val reservationTime = ThreadLocalRandom.current().nextInt(maxTime - minTime + 1) + minTime

        return Pair(reservationTime, Pair(listOfDays.get(idx), listOfDays.get(idx + reservationTime)))
    }

    private fun getRandomRoomSize(): Int {
        return ThreadLocalRandom.current().nextInt(maxSize - minSize + 1) + minSize
    }

    fun getRandomClient(dateRange: Pair<Date, Date>?): ReservationDatesDto {
        val (duration, dates) = getRandomDates(dateRange)
        val (d1, d2) = dates
        return ReservationDatesDto(
                startDate = d1, endDate = d2, roomSize = getRandomRoomSize(), days = duration
        )
    }

}

