package com.wjaronski.cassandrademo.repository

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.BatchStatement
import com.datastax.oss.driver.api.core.cql.BatchType
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.bindMarker
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable
import com.datastax.oss.driver.api.querybuilder.relation.Relation.column
import com.datastax.oss.driver.internal.core.type.DefaultTupleType
import com.wjaronski.cassandrademo.conf.AppSettings
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.dto.ReservationDatesDto
import com.wjaronski.cassandrademo.model.dto.RoomReservationDto
import com.wjaronski.cassandrademo.repository.additional.PreprocessTable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.lang.NonNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class RoomReservationRepository(
        @NonNull private var cqlSession: CqlSession,

        @Qualifier("keyspace") @NonNull
        private var keyspaceName: CqlIdentifier,
        val appSettings: AppSettings

) : PreprocessTable(cqlSession, keyspaceName, appSettings) {
    private val logger by LoggerDelegate()

    /**
     *    select day, room_i from prereservations
     *    where year = ?
     *    and week in (?)
     *    and day >= ?
     *    and day <= ?
     */
    val selectRoomReservationsByRoomSize = mutableMapOf<Int, PreparedStatement>()

    /**
     *    update prereservations set room_i = room_i + ?
     *    where year = ?
     *    and week = ?
     *    and day = ?
     */
    val appendSetItemByRoomSize = mutableMapOf<Int, PreparedStatement>()
    val removeSetItemByRoomSize = mutableMapOf<Int, PreparedStatement>()
    private val tupleType = DefaultTupleType(mutableListOf(DataTypes.INT, DataTypes.UUID))

    init {
        createTable()
        prepareStatements()
    }

    private fun prepareStatements() {
        logger.debug("\tPreparing statements")

        for (i in minS..maxS) {
            selectRoomReservationsByRoomSize.put(i, cqlSession.prepare(
                    selectFrom(keyspaceName, C.TABLE_ROOM_RESERVATION)
                            .column(C.DAY) // todo needed ?
                            .column(C.ROOM_WITH_X_SPACES(i))
                            .where(
                                    column(C.YEAR).isEqualTo(bindMarker()),
                                    column(C.WEEK).`in`(bindMarker()),
                                    column(C.DAY).isGreaterThanOrEqualTo(bindMarker()),
                                    column(C.DAY).isLessThanOrEqualTo(bindMarker())
                            )
                            .build()))
//            cqlSession.prepare(update(keyspaceName, C.TABLE_ROOM_RESERVATION)
//                    .appendSetElement(C.ROOM_WITH_X_SPACES(1), literal(""))
//                    .whereColumn(C.YEAR).isEqualTo(bindMarker())
//                    .whereColumn(C.WEEK).isEqualTo(bindMarker())
//                    .whereColumn(C.DAY).isEqualTo(bindMarker())
//                    .build())
            val remove = "update ${C.TABLE_ROOM_RESERVATION} set ${C.ROOM_WITH_X_SPACES(i)} = ${C.ROOM_WITH_X_SPACES(i)} - ? " +
                    "where ${C.YEAR} = ? and " +
                    "${C.WEEK} = ? and " +
                    "${C.DAY} = ? ;"

            val append = "update ${C.TABLE_ROOM_RESERVATION} set ${C.ROOM_WITH_X_SPACES(i)} = ${C.ROOM_WITH_X_SPACES(i)} + ? " +
                    "where ${C.YEAR} = ? and " +
                    "${C.WEEK} = ? and " +
                    "${C.DAY} = ? ;"
            appendSetItemByRoomSize.put(i, cqlSession.prepare(append))
            removeSetItemByRoomSize.put(i, cqlSession.prepare(remove))

        }
    }

    private fun mapRowToListWihtTouples(row: Row): Optional<List<RoomReservationDto>> {
        val tupleSet = row.getSet(1, C.TUPLE_INT_UUID)
        if (tupleSet == null || tupleSet.isEmpty()) {
            return Optional.empty()
        }
        return Optional.of(tupleSet.map {
            RoomReservationDto(room = it.getInt(0), reservation = it.getUuid(1)!!)
        })
    }

    private fun createTable() {
        prepare(C.TABLE_ROOM_RESERVATION)

        var table = createTable(keyspaceName, C.TABLE_ROOM_RESERVATION)
                .ifNotExists()
                .withPartitionKey(C.YEAR, DataTypes.INT)
                .withPartitionKey(C.WEEK, DataTypes.INT)
                .withClusteringColumn(C.DAY, DataTypes.INT)

        for (i in minS..maxS) {
            table = table.withColumn(C.ROOM_WITH_X_SPACES(i), DataTypes.setOf(DataTypes.tupleOf(DataTypes.INT, DataTypes.UUID)))
        }

        cqlSession.execute(table.build())
        logger.debug("\tTable '{}' has been created (if needed)", C.TABLE_ROOM_RESERVATION.asInternal())
    }

    fun getRoomsReservations(dto: ReservationDatesDto): List<Optional<List<RoomReservationDto>>> {

        val c1 = Calendar.getInstance()
        val c2 = Calendar.getInstance()
        c1.time = dto.startDate
        c2.time = dto.endDate

        val (sW, sD) = dto.week(true)
        val (eW, eD) = dto.week(false)

        val resultRows = cqlSession.execute(
                selectRoomReservationsByRoomSize.getValue(dto.roomSize)
                        .boundStatementBuilder(
                                dto.year,
                                (sW..eW).toList(),
                                sD,
                                eD
                        ).build()
        )

        return resultRows.all().map { mapRowToListWihtTouples(it) }

    }

    fun appendRoomReservation(dto: RoomReservationDto) {
        val statement = appendSetItemByRoomSize.getValue(dto.dates!!.roomSize)
        modifyRoomReservation(dto, statement)
    }

    fun removeRoomReservation(dto: RoomReservationDto) {
        val statement = removeSetItemByRoomSize.getValue(dto.dates!!.roomSize)
        modifyRoomReservation(dto, statement)
    }

    private fun modifyRoomReservation(dto: RoomReservationDto, baseStatement: PreparedStatement) {
        val dates = dto.dates!!
        val tuple = DataTypes.tupleOf(DataTypes.INT, DataTypes.UUID).newValue(dto.room, dto.reservation)


        val c1 = Calendar.getInstance()
        val c2 = Calendar.getInstance()

        c1.time = dto.dates!!.startDate
        c2.time = dto.dates!!.endDate

        val tupleValue = tupleType.newValue()
        val set = setOf<com.datastax.oss.driver.api.core.data.TupleValue>(tuple)

        val batchStmts = BatchStatement.builder(BatchType.LOGGED)
        while (c1.before(c2)) {
            batchStmts.addStatement(baseStatement.bind(set,
                    c1.get(Calendar.YEAR),
                    c1.get(Calendar.WEEK_OF_YEAR),
                    c1.get(Calendar.DAY_OF_YEAR)))
            c1.add(Calendar.DAY_OF_YEAR, 1)
        }

        cqlSession.execute(batchStmts.build())
    }


}
