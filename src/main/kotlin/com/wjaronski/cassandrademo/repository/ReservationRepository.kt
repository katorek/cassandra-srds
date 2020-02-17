package com.wjaronski.cassandrademo.repository


import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.*
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable
import com.wjaronski.cassandrademo.conf.AppSettings
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.dto.ReservationInfoDto
import com.wjaronski.cassandrademo.repository.additional.PreprocessTable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.lang.NonNull
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Created by Wojciech Jaronski
 *
 */
@Repository
class ReservationRepository(
        @NonNull private var cqlSession: CqlSession,

        @Qualifier("keyspace") @NonNull
        private var keyspaceName: CqlIdentifier,
        var appSettings: AppSettings

) : PreprocessTable(cqlSession, keyspaceName, appSettings) {

    private val logger by LoggerDelegate()

    private lateinit var psInsertReservationInfo: PreparedStatement
    private lateinit var getReservationInfo: PreparedStatement
    private lateinit var deleteReservationInfo: PreparedStatement

    init {
        createTable()
        prepareStatements()
    }

    private fun prepareStatements() {
        logger.debug("\tPreparing statements")
        psInsertReservationInfo = cqlSession.prepare(
                insertInto(keyspaceName, C.TABLE_RESERVATION)
                        .value(C.UUID, bindMarker())
                        .value(C.DESCRIPTION, bindMarker())
                        .build())
        getReservationInfo = cqlSession.prepare(
                selectFrom(keyspaceName, C.TABLE_RESERVATION)
                        .column(C.DESCRIPTION)
                        .whereColumn(C.UUID).isEqualTo(bindMarker())
                        .build()
        )
        deleteReservationInfo = cqlSession.prepare(
                deleteFrom(keyspaceName, C.TABLE_RESERVATION)
                        .whereColumn(C.UUID).isEqualTo(bindMarker())
                        .build()
        )
    }

    fun getReservation(uuid: UUID): Optional<String> {
        val rs = cqlSession.execute(getReservationInfo.boundStatementBuilder().setUuid(0, uuid).build())
        val one = rs.one()
        if (one != null) {
            return Optional.of(one.getString(0).toString())
        }
        return Optional.empty()
    }

    fun insertReservation(infoDto: ReservationInfoDto): UUID {
        val stms = psInsertReservationInfo.boundStatementBuilder(infoDto.uuid, infoDto.description).build()
        cqlSession.execute(stms)
        return infoDto.uuid
    }

    private fun createTable() {
        prepare(C.TABLE_RESERVATION)
        cqlSession.execute(
                createTable(keyspaceName, C.TABLE_RESERVATION)
                        .ifNotExists()
                        .withPartitionKey(C.UUID, DataTypes.UUID)
                        .withColumn(C.DESCRIPTION, DataTypes.TEXT)
                        .build())
        logger.debug("\tTable '{}' has been created (if needed)", C.TABLE_RESERVATION.asInternal())
    }

    fun removeReservationInfo(uuid: UUID) {
        cqlSession.execute(deleteReservationInfo.bind(uuid))
    }

}
