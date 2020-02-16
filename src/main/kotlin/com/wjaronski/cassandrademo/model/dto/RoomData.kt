package com.wjaronski.cassandrademo.model.dto

import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate

data class RoomData(
        val year: Int,
        val month: Int,
        val rooms: Map<Int, Set<Int>>
) {


    companion object {
        private val logger by LoggerDelegate()

        fun fromCSV(line: String): RoomData {
            val tokens = line.split(";")
            val year = tokens[0].toInt()
            val month = tokens[1].toInt()

            val map = mutableMapOf<Int, Set<Int>>()

            for ((idx, room) in tokens.drop(2).withIndex()) {
                map.put(idx + 1, setFromString(room))
            }

            return RoomData(
                    year = year,
                    month = month,
                    rooms = map
            )
        }

        private fun setFromString(str: String): Set<Int> {
            return str.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.toInt() }.toSet()
        }
    }
}