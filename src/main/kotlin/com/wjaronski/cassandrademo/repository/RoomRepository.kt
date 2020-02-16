package com.wjaronski.cassandrademo.repository


import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.*
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.*
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable
import com.datastax.oss.driver.api.querybuilder.relation.Relation.column
import com.wjaronski.cassandrademo.conf.AppSettings
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.dto.RoomAvailabilityDto
import com.wjaronski.cassandrademo.model.dto.RoomData
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
class RoomRepository(
        @NonNull private var cqlSession: CqlSession,

        @Qualifier("keyspace") @NonNull
        private var keyspaceName: CqlIdentifier,
        var appSettings: AppSettings

) : PreprocessTable(cqlSession, keyspaceName, appSettings) {

    private val logger by LoggerDelegate()

    private lateinit var psInsertRoomAvailability: PreparedStatement
    private lateinit var psGetRoomAvailability: PreparedStatement

    private val psRoomsByYearAndMonth = mutableMapOf<Int, PreparedStatement>()

    init {
        createTable()
        prepareStatements()
    }

    fun getRoomAvailability(dto: RoomAvailabilityDto): Optional<Set<Int>> {
        validateDto(dto)
        val rs = cqlSession.execute(psRoomsByYearAndMonth.get(dto.howManyPeople)!!.bind(dto.year, dto.month))
        val one = rs.one()
        if (one == null) {
            logger.debug("No data")
            return Optional.empty()
        }
        return Optional.of(mapRowToSet(one))
    }

    private fun validateDto(dto: RoomAvailabilityDto) {
        if (!(dto.howManyPeople in (minS..maxS)))
            throw RuntimeException("Room size not in range")
    }

    private fun mapRowToSet(row: Row): Set<Int> {
        logger.debug(row.toString())
        logger.info(row.toString())
        return row.getSet(0, Int::class.javaObjectType) as Set<Int>
//        val obj = row.getObject(0)
//        return row.getObject(0) as Set<Int>

//        return row.getSet(0, Integer::class.java) as Set<Int>

    }

    private fun prepareStatements() {
        logger.debug("\tPreparing statements")
        var insertTmp = insertInto(keyspaceName, C.TABLE_ROOMS)
                .value(C.YEAR, bindMarker(C.YEAR))
                .value(C.MONTH, bindMarker(C.MONTH))


//        var selectTmp = QueryBuilder.selectFrom(keyspaceName, C.TABLE_ROOMS)


        // add necessary columns to statements
        for (i in minS..maxS) {
            insertTmp = insertTmp.value(C.ROOM_WITH_X_SPACES(i), bindMarker(C.ROOM_WITH_X_SPACES(i)))

            val psI = selectFrom(keyspaceName, C.TABLE_ROOMS)
                    .column(C.ROOM_WITH_X_SPACES(i))
                    .where(column(C.YEAR).isEqualTo(bindMarker(C.YEAR)),
                            column(C.MONTH).isEqualTo(bindMarker(C.MONTH)))
                    .build()
            psRoomsByYearAndMonth.put(i, cqlSession.prepare(psI))

        }


        psInsertRoomAvailability = cqlSession.prepare(insertTmp.build())

    }

    /**
     *     CREATE TABLE srds.rooms (
     *     year int,
     *     month smallint,
     *     room_X set<smallint>,
     *     room_(X+1) set<smallint>,
     *        ...
     *     room_(Y-1) set<smallint>,
     *     room_Y set<smallint>,
     *     where room_Z represents set of rooms with Z spaces in it
     *     X < Y
     *     Default: X = 1, Y = 5
     * )
     */
    private fun createTable() {
        prepare(C.TABLE_ROOMS)

        var baseTable = createTable(keyspaceName, C.TABLE_ROOMS)
                .ifNotExists()
                .withPartitionKey(C.YEAR, DataTypes.INT)
                .withPartitionKey(C.MONTH, DataTypes.INT)
        // add columns
        for (i in minS..maxS)
            baseTable = baseTable.withColumn(C.ROOM_WITH_X_SPACES(i), DataTypes.setOf(DataTypes.INT))

        cqlSession.execute(baseTable.build())
        logger.debug("\tTable '{}' has been created (if needed)", C.TABLE_ROOMS.asInternal())
    }

    fun insertData(roomData: List<RoomData>) {
//        psInsertRoomAvailability.bind()
        val stms = roomData.map { prepareRoomData(it) }.toList()
        logger.debug("+ Inserting {} row to table ROOMS", stms.size)
        cqlSession.execute(BatchStatement.builder(DefaultBatchType.LOGGED)
                .addStatements(stms)
                .build())
    }

    private fun prepareRoomData(data: RoomData): BoundStatement {
//        var tmp = psInsertRoomAvailability.bind(data.year, data.month)
        var tmp = psInsertRoomAvailability.boundStatementBuilder()
        tmp.setInt(C.YEAR, data.year)
        tmp.setInt(C.MONTH, data.month)
        for (i in minS..maxS) {
            tmp.setSet(C.ROOM_WITH_X_SPACES(i), data.rooms.get(i).orEmpty(), Int::class.javaObjectType)
        }
        return tmp.build()
    }

}
