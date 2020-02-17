package com.wjaronski.cassandrademo.service

import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import org.springframework.stereotype.Service

/**
 * Created by Wojciech Jaronski
 *
 */

@Service
class StressTestService(
        service: FlowService
) {
    val logger by LoggerDelegate()

}