package com.wjaronski.cassandrademo.conf

import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "appsettings")
data class AppSettings(
        val cass: CassandraSettings,
        val room: RoomProperties,
        val initData: String
) {

    val logger by LoggerDelegate()

    data class CassandraSettings(
            val contactPoint: String,
            val port: Int,
            val keyspaceName: String,
            val localDataCenterName: String,
            val dropSchema: Boolean,
            val tables: String
    )

    data class RoomProperties(
            val minSize: Int,
            val maxSize: Int
    )

    init {
        logger.debug("Properties loaded: {}", this)
    }
}