package com.wjaronski.cassandrademo.repository

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.*
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable
import com.datastax.oss.driver.api.querybuilder.relation.Relation.column
import com.wjaronski.cassandrademo.conf.AppSettings
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.dto.ReservationDatesDto
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
class PrereservationRepository(
        @NonNull private var cqlSession: CqlSession,

        @Qualifier("keyspace") @NonNull
        private var keyspaceName: CqlIdentifier,
        var appSettings: AppSettings

) : PreprocessTable(cqlSession, keyspaceName, appSettings) {


    private val logger by LoggerDelegate()

    /**
     *    update prereservations set room_X=room_X+1
     *    where year=x1 and
     *    week=x2 and
     *    day day = x3;
     */
    private val incrementCounterDayByRoomSizeMap = mutableMapOf<Int, PreparedStatement>()
    private val decrementCounterDayByRoomSizeMap = mutableMapOf<Int, PreparedStatement>()

    /**
     *    select room_X (cnt) form ...
     *    where year=x1 and
     *    week=x2 and
     *    day >= x3 and day <= x4
     */
    private val selectMapByRoomSize = mutableMapOf<Int, PreparedStatement>()

    init {
        createTable()
        prepareStatements()
    }

    private fun createTable() {
        prepare(C.TABLE_PRERESERVATION)

        var baseTable = createTable(keyspaceName, C.TABLE_PRERESERVATION)
                .ifNotExists()
                .withPartitionKey(C.YEAR, DataTypes.INT)
                .withPartitionKey(C.WEEK, DataTypes.INT)
                .withClusteringColumn(C.DAY, DataTypes.INT)
        // add columns
        for (i in minS..maxS)
            baseTable = baseTable.withColumn(C.ROOM_WITH_X_SPACES(i), DataTypes.COUNTER)

        cqlSession.execute(baseTable.build())
        logger.debug("+ Table '{}' has been created (if needed)", C.TABLE_PRERESERVATION.asInternal())
    }

    private fun prepareStatements() {
        logger.debug("\tPreparing statements")

        for (i in minS..maxS) {

            incrementCounterDayByRoomSizeMap.put(i,
                    cqlSession.prepare(QueryBuilder.update(keyspaceName, C.TABLE_PRERESERVATION)
                            .increment(C.ROOM_WITH_X_SPACES(i))
                            .where(
                                    column(C.YEAR).isEqualTo(bindMarker()),
                                    column(C.WEEK).isEqualTo(bindMarker()),
                                    column(C.DAY).isEqualTo(bindMarker())
                            )
                            .build()))
            decrementCounterDayByRoomSizeMap.put(i,
                    cqlSession.prepare(QueryBuilder.update(keyspaceName, C.TABLE_PRERESERVATION)
                            .decrement(C.ROOM_WITH_X_SPACES(i))
                            .where(
                                    column(C.YEAR).isEqualTo(bindMarker()),
                                    column(C.WEEK).isEqualTo(bindMarker()),
                                    column(C.DAY).isEqualTo(bindMarker())
                            )
                            .build()))
            selectMapByRoomSize.put(i, cqlSession.prepare(QueryBuilder.selectFrom(keyspaceName, C.TABLE_PRERESERVATION)

                    .column(C.ROOM_WITH_X_SPACES(i))
                    .where(
                            column(C.YEAR).isEqualTo(bindMarker()),
                            column(C.WEEK).isEqualTo(bindMarker()),
                            column(C.DAY).isGreaterThanOrEqualTo(bindMarker()),
                            column(C.DAY).isLessThanOrEqualTo(bindMarker())
                    )
                    .build()))
        }

    }

    fun getCounter(dto: ReservationDatesDto): Optional<Collection<Long>> {
        val stmt = selectMapByRoomSize.get(dto.roomSize)
        if (stmt != null) {
            val (week, startDay) = dto.week(true)
            val endDay = dto.week(false).second

            val rs = cqlSession.execute(
                    stmt.boundStatementBuilder()
                            .setInt(0, dto.year!!)
                            .setInt(1, week)
                            .setInt(2, startDay)
                            .setInt(3, endDay)
                            .build())
            return Optional.of(rs.all().map { mapRowToCounter(it) }.toList())

        }
        return Optional.empty()
    }

    fun decrementCounter(dto: ReservationDatesDto) {
        updateCounter(dto, decrementCounterDayByRoomSizeMap.getValue(dto.roomSize))
    }

    fun incrementCounter(dto: ReservationDatesDto) {
        updateCounter(dto, incrementCounterDayByRoomSizeMap.getValue(dto.roomSize))
    }

    private fun updateCounter(dto: ReservationDatesDto, statement: PreparedStatement) {
        val stmts = arrayListOf<BoundStatement>()
        val c1 = Calendar.getInstance()
        val c2 = Calendar.getInstance()
        c1.time = dto.startDate
        c2.time = dto.endDate

        while (c1.before(c2)) {
            val t = statement.boundStatementBuilder()
            stmts.add(
                    t.setInt(0, c1.get(Calendar.YEAR))
                            .setInt(1, c1.get(Calendar.WEEK_OF_YEAR))
                            .setInt(2, c1.get(Calendar.DAY_OF_YEAR))
                            .build())
            c1.add(Calendar.DAY_OF_YEAR, 1)
        }
//        logger.info("Days {}", stmts.size)
//        logger.info("Updating counters[{}] from {} to {}", dto.roomSize, dto.startDate, dto.endDate)
        cqlSession.execute(BatchStatement.builder(DefaultBatchType.COUNTER)
                .addStatements(stmts)
                .build())
    }

    fun mapRowToCounter(row: Row): Long {
        return row.getLong(0)
    }
}