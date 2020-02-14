package com.wjaronski.cassandrademo.repository

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.lang.NonNull
import org.springframework.stereotype.Repository

@Repository
class ReservationRepository(
        @NonNull private var cqlSession: CqlSession,

        @Qualifier("keyspace") @NonNull
        private var keyspaceName: CqlIdentifier

) {


    private val logger by LoggerDelegate()


    init {
        createTables()
        prepareStatements()

        logger.info("Application Initialized")
    }

    private fun prepareStatements() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun createTables() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
