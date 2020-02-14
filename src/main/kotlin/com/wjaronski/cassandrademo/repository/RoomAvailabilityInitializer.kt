package com.wjaronski.cassandrademo.repository

import com.wjaronski.cassandrademo.conf.AppSettings
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.dto.RoomData
import org.springframework.stereotype.Component
import java.io.File

/**
 * Created by Wojciech Jaronski
 *
 */

@Component
class RoomAvailabilityInitializer(
        val roomRepository: RoomRepository,
        val appSettings: AppSettings

) {
    private val logger by LoggerDelegate()

    private val minSizeOfRoom = appSettings.room.minSize
    private val maxSizeOfRoom = appSettings.room.maxSize
    private val fileData = "data.csv" //appSettings.initData

    val data = mutableListOf<RoomData>()

    init {
        loadData()
        insertData()
    }

    private fun insertData() {
        roomRepository.insertData(data)
    }

    fun loadData() {
        File(this.javaClass.classLoader.getResource(fileData).getFile())
                .forEachLine { data.add(RoomData.fromCSV(it)) }
    }


}

