package com.wjaronski.cassandrademo.service

import com.wjaronski.cassandrademo.conf.AppSettings
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.dto.RoomData
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File

/**
 * Created by Wojciech Jaronski
 *
 */

@Component
@Profile("initData")
class DataInitializer(
        val reservationService: ReservationService,
        val appSettings: AppSettings
) {
    private val logger by LoggerDelegate()

    private val fileData = appSettings.initData //appSettings.initData

    private val data = mutableListOf<RoomData>()

    init {
        loadData()
        insertData()
//        testPrereservation()
    }

    private fun insertData() {
        logger.debug("Inserting data")
        reservationService.insertData(data)
    }

    private fun loadData() {
        logger.debug("Loading data")
        File(this.javaClass.classLoader.getResource(fileData).getFile())
                .forEachLine { data.add(RoomData.fromCSV(it)) }
    }

    /*
    private fun testPrereservation() {
        logger.debug("Testing counters")
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()

        startDate.set(2020, 0, 1)
        endDate.set(2020, 0, 10)

        logger.debug("\tIncrementing {}, from {} to {}", 2, startDate.time, endDate.time)
        reservationService.incrementCounter(ReservationDatesDto(
                roomSize = 2,
                startDate = startDate.time,
                endDate = endDate.time
        ))

        startDate.set(2020, 0, 5)
        endDate.set(2020, 0, 13)

        logger.debug("\tDecrementing {}, from {} to {}", 4, startDate.time, endDate.time)
        reservationService.decrementCounter(ReservationDatesDto(
                roomSize = 4,
                startDate = startDate.time,
                endDate = endDate.time
        ))
    }
    */

}

