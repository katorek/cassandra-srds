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
            logger.debug("{}", line)
            val tokens = line.split(";")
            val year = tokens[0].toInt()
            val month = tokens[1].toInt()

            val map = mutableMapOf<Int, Set<Int>>()

            for ((idx, room) in tokens.drop(2).withIndex()) {
                logger.debug("I:{} room: {}", idx, room)
                map.put(idx, setFromString(room))
            }

            return RoomData(
                    year = year,
                    month = month,
                    rooms = map
            )
        }

        private fun setFromString(str: String): Set<Int> {
            // [101,102,103,104,105]
            return str.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.toInt() }.toSet()
        }
    }
}