package com.wjaronski.cassandrademo.model.dto

import java.util.*

/**
 *     roomSize
 *     startDate
 *     endDate
 */
data class ReservationDatesDto(
        val roomSize: Int,
        val startDate: Date,
        val endDate: Date
) {
    var year: Int? = 0

    companion object {
        val calendar = Calendar.getInstance()
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
