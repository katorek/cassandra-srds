package com.wjaronski.cassandrademo.service

import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.ProgressStatus
import com.wjaronski.cassandrademo.model.dto.ReservationDatesDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.streams.toList

/**
 * Created by Wojciech Jaronski
 *
 */

@Service
class StressTestService(
        private val service: FlowService,
        private val clientGenerator: ClientGenerator
) {
    val logger by LoggerDelegate()

    val threadsList = ConcurrentHashMap<UUID, Runnable>()
    val counterSuccess = AtomicInteger(0)
    val counterFailure = AtomicInteger(0)
    val counterUnknown = AtomicInteger(0)
    val stats = ConcurrentSkipListSet<ReservationDatesDto>()

    fun testX(load: Int, dateRange: Pair<Date, Date>?): String {


        stats.clear()
        counterSuccess.set(0)
        counterFailure.set(0)
        counterUnknown.set(0)
        val startTime = System.currentTimeMillis()

        threadsList.putAll(IntStream.rangeClosed(0, load).mapToObj { clientGenerator.getRandomClient(dateRange) }.map { thread(it) }.toList())
        threadsList.forEach { it.value.run() }
//        val results =
        runBlocking {
            while (threadsList.isNotEmpty()) {
                delay(500)
            }
        }
        val endTime = System.currentTimeMillis() - startTime

        return processStats(endTime)
    }


    fun processStats(time: Long): String {
//        val duration = hashMapOf<Int, Int>()

        val durationsProgres = HashMap<Int, HashMap<ProgressStatus, Int>>()
        val roomSizeProgres = HashMap<Int, HashMap<ProgressStatus, Int>>()
        val overallStatus = HashMap<ProgressStatus, Int>()

        stats.forEach {

            val m = overallStatus.getOrDefault(it.result, 0)
            overallStatus.put(it.result, m + 1)


            val m1 = durationsProgres.getOrDefault(it.days, HashMap())
            val m11 = m1.getOrDefault(it.result, 0)
            m1.put(it.result, m11 + 1)
            durationsProgres.put(it.days!!, m1)

            val m2 = roomSizeProgres.getOrDefault(it.roomSize, HashMap())
            val m22 = m2.getOrDefault(it.result, 0)
            m2.put(it.result, m22 + 1)
            roomSizeProgres.put(it.roomSize, m2)

        }

        val sb = StringBuilder()

        logger.info("---------- SUMMARY STATISTICS ----------".also { sb.append(it).append("\n") })
        logger.info("".also { sb.append(it).append("\n") })
        logger.info("Execution time: ${time}ms | ${TimeUnit.MILLISECONDS.toSeconds(time)}s | ${TimeUnit.MILLISECONDS.toMinutes(time)}min".also { sb.append(it).append("\n") })
        logger.info("".also { sb.append(it).append("\n") })
        logger.info("Runned: ${stats.size}\t${overallStatus.map { (k2, v2) -> "$k2[$v2]" }.joinToString("\t", "\t")}".also { sb.append(it).append("\n") })
        logger.info("".also { sb.append(it).append("\n") })
        logger.info("---------- DURATIONS IN DAYS -----------".also { sb.append(it).append("\n") })
        logger.info("".also { sb.append(it).append("\n") })
        durationsProgres.forEach { k1, v1 ->
            logger.info("Duration: $k1\t${v1.map { (k2, v2) -> "$k2[$v2]" }.joinToString("\t", "\t")}".also { sb.append(it).append("\n") })
        }
        logger.info("".also { sb.append(it).append("\n") })
        logger.info("---------- ROOM SIZE -------------------".also { sb.append(it).append("\n") })
        logger.info("".also { sb.append(it).append("\n") })
        roomSizeProgres.forEach { k1, v1 ->
            logger.info("Room size: $k1\t${v1.map { (k2, v2) -> "$k2[$v2]" }.joinToString("\t", "\t")}".also { sb.append(it).append("\n") })
        }
        logger.info("".also { sb.append(it).append("\n") })
        logger.info("----------- END OF STATISTICS ----------".also { sb.append(it).append("\n") })
        logger.info("".also { sb.append(it).append("\n") })

        return sb.toString()
    }

    fun thread(data: ReservationDatesDto): Pair<UUID, Runnable> {
        val uuid = UUID.randomUUID()

        return Pair(uuid, thread(start = false) {
            //            logger.debug("\t${Thread.currentThread().name} STARTED")
            val result = service.doReservation(data)
            if (result.toString().contains("Successfully")) {
                counterSuccess.incrementAndGet()
                data.result = ProgressStatus.SUCCESS
            } else if (result.toString().contains("Unknown")) {
                counterUnknown.incrementAndGet()
                data.result = ProgressStatus.UNKNOWN
            } else {
                counterFailure.incrementAndGet()
                data.result = ProgressStatus.FAILURE
            }
//            logger.debug("\t${Thread.currentThread().name} ENDED. RESULT = $result")
            stats.add(data)
            threadsList.remove(uuid)
        })
    }

}