package com.wjaronski.cassandrademo.model.dto

import com.wjaronski.cassandrademo.model.ProgressStatus
import java.util.*

/**
 *     roomSize
 *     startDate
 *     endDate
 */
data class ReservationDatesDto(
        val roomSize: Int,
        val startDate: Date,
        val endDate: Date,
        var days: Int? = null
) : Comparable<ReservationDatesDto> {
    override fun compareTo(other: ReservationDatesDto): Int {
        return COMPARATOR.compare(this, other)
    }

    val uuid = UUID.randomUUID()

    var year: Int? = 0
    var result: ProgressStatus = ProgressStatus.INIT

    companion object {
        val calendar = Calendar.getInstance()
        private val COMPARATOR =
                Comparator.comparingInt<ReservationDatesDto> { it.roomSize }
                        .thenComparing { o1: ReservationDatesDto?, o2: ReservationDatesDto? -> o1!!.uuid.compareTo(o2!!.uuid) }
                        .thenComparing { o1: ReservationDatesDto?, o2: ReservationDatesDto? -> o1!!.startDate.compareTo(o2!!.startDate) }
                        .thenComparing { o1: ReservationDatesDto?, o2: ReservationDatesDto? -> o1!!.endDate.compareTo(o2!!.endDate) }
    }

    /**
     *    Pair (X, Y), where
     *    X - WEEK_OF_YEAR
     *    Y - DAY_OF_YEAR
     */
    fun week(isStartDate: Boolean): Pair<Int, Int> {
        when (isStartDate) {
            true -> return weekFromDate(startDate)
            false -> return weekFromDate(endDate)
        }
    }

    private fun weekFromDate(date: Date): Pair<Int, Int> {
        calendar.time = date
        year = calendar.get(Calendar.YEAR)
        return Pair(calendar.get(Calendar.WEEK_OF_YEAR), calendar.get(Calendar.DAY_OF_YEAR))
    }

    override fun toString(): String {
        return "{\"startDate\":$startDate, \"endDate\":$endDate, \"roomSize\":$roomSize}"
    }
}
