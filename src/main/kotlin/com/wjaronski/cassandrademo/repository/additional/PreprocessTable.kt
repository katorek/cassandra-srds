package com.wjaronski.cassandrademo.repository.additional

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.wjaronski.cassandrademo.conf.AppSettings
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.constants.CqlConstants
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.lang.NonNull

/**
 * Created by Wojciech Jaronski
 *
 */

open class PreprocessTable(
        @NonNull private var cqlSession: CqlSession,

        @Qualifier("keyspace") @NonNull
        private var keyspaceName: CqlIdentifier,
        private val appSettings: AppSettings
) {
    private val logger by LoggerDelegate()

    val minS = appSettings.room.minSize
    val maxS = appSettings.room.maxSize
    val C = CqlConstants

    fun prepare(tableIdentifier: CqlIdentifier) {
        logger.debug("Preprocessing table '{}' ", tableIdentifier.asInternal())
        when (appSettings.cass.tables) {
            "drop" -> {
                logger.debug("\tDropping table '{}'", tableIdentifier.asInternal())
                cqlSession.execute(SchemaBuilder.dropTable(keyspaceName, tableIdentifier).ifExists().build())
            }
            "truncate" -> {
                logger.debug("\tTruncating table '{}'", tableIdentifier.asInternal())
                cqlSession.execute(QueryBuilder.truncate(keyspaceName, tableIdentifier).build())
            }
            else -> {
                logger.debug("\tUnrecognized option '{}'! Doing nothing", appSettings.cass.tables)
            }
        }
    }
}