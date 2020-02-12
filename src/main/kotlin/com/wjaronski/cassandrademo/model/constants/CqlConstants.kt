package com.wjaronski.cassandrademo.model.constants

import com.datastax.oss.driver.api.core.CqlIdentifier


/**
 * Created by Wojciech Jaronski
 *
 */

class CqlConstants {
    companion object {
        //        todo validation ?
//        @Value("\${limits.room.min:1}")
//        private val room_min: Long = 1
//
//        @Value("\${limits.room.max:3}")
//        private val room_max: Long = 3
        //        Tables
        val TABLE_ROOMS = CqlIdentifier.fromCql("rooms")
        val TABLE_PRERESERVATION = CqlIdentifier.fromCql("prereservations")
        val TABLE_RESERVATION = CqlIdentifier.fromCql("reservations")

        //        Fields
        val YEAR = CqlIdentifier.fromCql("year")
        val MONTH = CqlIdentifier.fromCql("month")
        val DAY = CqlIdentifier.fromCql("day")

        /**
         *  Helper function
         */
        private fun identifierOf(string: String): CqlIdentifier {
            return CqlIdentifier.fromCql(string)
        }

//        fun PERSON(i: Int): CqlIdentifier {
//            return identifierOf("person_$i")
//        }

        fun ROOM_WITH_X_SPACES(x: Int): CqlIdentifier {
            return identifierOf("room_$x")
        }

//        fun BOOKED(i: Int): CqlIdentifier {
//            return identifierOf("booked_$i")
//        }
    }
}