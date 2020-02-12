package com.wjaronski.cassandrademo.repository

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import com.wjaronski.cassandrademo.model.constants.CqlConstants
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.lang.NonNull
import org.springframework.stereotype.Repository
import javax.annotation.PreDestroy

/**
 * Created by Wojciech Jaronski
 *
 */
@Repository
class RoomRepository(
        @NonNull private var cqlSession: CqlSession,

        @Qualifier("keyspace") @NonNull
        private var keyspaceName: CqlIdentifier

) {
    @Value("\${limits.room.min:1}")
    private val minSizeOfRoom: Int = 1

    @Value("\${limits.room.max:5}")
    private val maxSizeOfRoom: Int = 5

    private val logger by LoggerDelegate()
    private val C = CqlConstants

    init {
        createTables()
        prepareStatements()


        logger.info("Application Initialized")
    }

    private fun prepareStatements() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
    private fun createTables() {

        var baseTable = createTable(keyspaceName, C.TABLE_ROOMS)
                .ifNotExists()
                .withPartitionKey(C.YEAR, DataTypes.INT)
                .withPartitionKey(C.MONTH, DataTypes.SMALLINT)
        // add columns
        for (i in minSizeOfRoom..maxSizeOfRoom)
            baseTable = baseTable.withColumn(C.ROOM_WITH_X_SPACES(i), DataTypes.setOf(DataTypes.SMALLINT))

        cqlSession.execute(baseTable.build())
        logger.debug("+ Table '{}' has been created (if needed)", C.TABLE_ROOMS.asInternal())
    }

    fun createReservationTables() {

        /**
         * Create TYPE 'Address' if not exists
         *
         * CREATE TYPE reservation.address (
         * street text,
         * city text,
         * state_or_province text,
         * postal_code text,
         * country text
         * );
         */
//        cqlSession.execute(
//                createType(keyspaceName, TYPE_ADDRESS)
//                        .ifNotExists()
//                        .withField(STREET, DataTypes.TEXT)
//                        .withField(CITY, DataTypes.TEXT)
//                        .withField(STATE_PROVINCE, DataTypes.TEXT)
//                        .withField(POSTAL_CODE, DataTypes.TEXT)
//                        .withField(COUNTRY, DataTypes.TEXT)
//                        .build())
//        logger.debug("+ Type '{}' has been created (if needed)", TYPE_ADDRESS.asInternal())

        /**
         * CREATE TABLE reservation.reservations_by_hotel_date (
         * hotel_id text,
         * start_date date,
         * end_date date,
         * room_number smallint,
         * confirmation_number text,
         * guest_id uuid,
         * PRIMARY KEY ((hotel_id, start_date), room_number)
         * ) WITH comment = 'Q7. Find reservations by hotel and date';
         */


//        cqlSession.execute(createTable(keyspaceName, C.TABLE_ROOMS)
//                .ifNotExists()
//                .withPartitionKey(C.YEAR, DataTypes.INT)
//                .withPartitionKey(C.MONTH, DataTypes.SMALLINT)
//                .withColumn(C.ROOM_WITH_X_SPACES(1), DataTypes.setOf(DataTypes.SMALLINT))
//                .withColumn(C.ROOM_WITH_X_SPACES(2), DataTypes.setOf(DataTypes.SMALLINT))
//                .withColumn(C.ROOM_WITH_X_SPACES(3), DataTypes.setOf(DataTypes.SMALLINT))
//                .withColumn(C.ROOM_WITH_X_SPACES(4), DataTypes.setOf(DataTypes.SMALLINT))
//                .withColumn(C.ROOM_WITH_X_SPACES(5), DataTypes.setOf(DataTypes.SMALLINT))
//
//
//        )
//
//        cqlSession.execute(createTable(keyspaceName, TABLE_RESERVATION_BY_HOTEL_DATE)
//                .ifNotExists()
//                .withPartitionKey(HOTEL_ID, DataTypes.TEXT)
//                .withPartitionKey(START_DATE, DataTypes.DATE)
//                .withClusteringColumn(ROOM_NUMBER, DataTypes.SMALLINT)
//                .withColumn(END_DATE, DataTypes.DATE)
//                .withColumn(CONFIRMATION_NUMBER, DataTypes.TEXT)
//                .withColumn(GUEST_ID, DataTypes.UUID)
//                .withClusteringOrder(ROOM_NUMBER, ClusteringOrder.ASC)
//                .withComment("Q7. Find reservations by hotel and date")
//                .build())
//        logger.debug("+ Table '{}' has been created (if needed)", TABLE_RESERVATION_BY_HOTEL_DATE.asInternal())
//
//        /**
//         * CREATE TABLE reservation.reservations_by_confirmation (
//         * confirmation_number text PRIMARY KEY,
//         * hotel_id text,
//         * start_date date,
//         * end_date date,
//         * room_number smallint,
//         * guest_id uuid
//         * );
//         */
//        cqlSession.execute(createTable(keyspaceName, TABLE_RESERVATION_BY_CONFI)
//                .ifNotExists()
//                .withPartitionKey(CONFIRMATION_NUMBER, DataTypes.TEXT)
//                .withColumn(HOTEL_ID, DataTypes.TEXT)
//                .withColumn(START_DATE, DataTypes.DATE)
//                .withColumn(END_DATE, DataTypes.DATE)
//                .withColumn(ROOM_NUMBER, DataTypes.SMALLINT)
//                .withColumn(GUEST_ID, DataTypes.UUID)
//                .build())
//        logger.debug("+ Table '{}' has been created (if needed)", TABLE_RESERVATION_BY_CONFI.asInternal())
//
//        /**
//         * CREATE TABLE reservation.reservations_by_guest (
//         * guest_last_name text,
//         * hotel_id text,
//         * start_date date,
//         * end_date date,
//         * room_number smallint,
//         * confirmation_number text,
//         * guest_id uuid,
//         * PRIMARY KEY ((guest_last_name), hotel_id)
//         * ) WITH comment = 'Q8. Find reservations by guest name';
//         */
//        cqlSession.execute(createTable(keyspaceName, TABLE_RESERVATION_BY_GUEST)
//                .ifNotExists()
//                .withPartitionKey(GUEST_LAST_NAME, DataTypes.TEXT)
//                .withClusteringColumn(HOTEL_ID, DataTypes.TEXT)
//                .withColumn(START_DATE, DataTypes.DATE)
//                .withColumn(END_DATE, DataTypes.DATE)
//                .withColumn(ROOM_NUMBER, DataTypes.SMALLINT)
//                .withColumn(CONFIRMATION_NUMBER, DataTypes.TEXT)
//                .withColumn(GUEST_ID, DataTypes.UUID)
//                .withComment("Q8. Find reservations by guest name")
//                .build())
//        logger.debug("+ Table '{}' has been created (if needed)", TABLE_RESERVATION_BY_GUEST.asInternal())
//
//        /**
//         * CREATE TABLE reservation.guests (
//         * guest_id uuid PRIMARY KEY,
//         * first_name text,
//         * last_name text,
//         * title text,
//         * emails set<text>,
//         * phone_numbers list<text>,
//         * addresses map<text></text>, frozen<address>>,
//         * confirmation_number text
//         * ) WITH comment = 'Q9. Find guest by ID';
//        </address></text></text> */
//        val udtAddressType = cqlSession.metadata.getKeyspace(keyspaceName).get() // Retrieving KeySpaceMetadata
//                .getUserDefinedType(TYPE_ADDRESS).get()        // Looking for UDT (extending DataType)
//        cqlSession.execute(createTable(keyspaceName, TABLE_GUESTS)
//                .ifNotExists()
//                .withPartitionKey(GUEST_ID, DataTypes.UUID)
//                .withColumn(FIRSTNAME, DataTypes.TEXT)
//                .withColumn(LASTNAME, DataTypes.TEXT)
//                .withColumn(TITLE, DataTypes.TEXT)
//                .withColumn(EMAILS, DataTypes.setOf(DataTypes.TEXT))
//                .withColumn(PHONE_NUMBERS, DataTypes.listOf(DataTypes.TEXT))
//                .withColumn(ADDRESSES, DataTypes.mapOf(DataTypes.TEXT, udtAddressType, true))
//                .withColumn(CONFIRMATION_NUMBER, DataTypes.TEXT)
//                .withComment("Q9. Find guest by ID")
//                .build())
//        logger.debug("+ Table '{}' has been created (if needed)", TABLE_GUESTS.asInternal())
//        logger.info("Schema has been successfully initialized.")
    }

    @PreDestroy
    public fun cleanUp() {

    }

}
