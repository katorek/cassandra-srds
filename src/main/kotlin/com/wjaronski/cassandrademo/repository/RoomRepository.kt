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
import com.wjaronski.cassandrademo.model.constants.CqlConstants
import com.wjaronski.cassandrademo.model.dto.RoomAvailabilityDto
import com.wjaronski.cassandrademo.model.dto.RoomData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.lang.NonNull
import org.springframework.stereotype.Repository
import java.util.*
import javax.annotation.PreDestroy

/**
 * Created by Wojciech Jaronski
 *
 */
@Repository
class RoomRepository(
        @NonNull private var cqlSession: CqlSession,

        @Qualifier("keyspace") @NonNull
        private var keyspaceName: CqlIdentifier,
        val appSettings: AppSettings

) {
    private val minSizeOfRoom = appSettings.room.minSize
    private val maxSizeOfRoom = appSettings.room.maxSize

    private val logger by LoggerDelegate()
    private val C = CqlConstants

    private lateinit var psInsertRoomAvailability: PreparedStatement
    private lateinit var psGetRoomAvailability: PreparedStatement

    private val psRoomsByYearAndMonth = mutableMapOf<Int, PreparedStatement>()

    init {
        createTables()
        prepareStatements()


        logger.info("Application Initialized")
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
        if (!(dto.howManyPeople in (minSizeOfRoom..maxSizeOfRoom)))
            throw RuntimeException("Room size not in range")
    }

    private fun mapRowToSet(row: Row): Set<Int> {
        logger.debug(row.toString())
        logger.info(row.toString())
//        DataTypes.SMALLINT.
//                row.getSe

        //todo use Int::class.javaObjectType
        return row.getSet(0, Int::class.javaObjectType) as Set<Int>
//        val obj = row.getObject(0)
//        return row.getObject(0) as Set<Int>

//        return row.getSet(0, Integer::class.java) as Set<Int>

    }

    private fun prepareStatements() {
        var insertTmp = insertInto(keyspaceName, C.TABLE_ROOMS)
                .value(C.YEAR, bindMarker(C.YEAR))
                .value(C.MONTH, bindMarker(C.MONTH))


//        var selectTmp = QueryBuilder.selectFrom(keyspaceName, C.TABLE_ROOMS)


        // add necessary columns to statements
        for (i in minSizeOfRoom..maxSizeOfRoom) {
            insertTmp = insertTmp.value(C.ROOM_WITH_X_SPACES(i), bindMarker(C.ROOM_WITH_X_SPACES(i)))

            val psI = selectFrom(keyspaceName, C.TABLE_ROOMS)
                    .column(C.ROOM_WITH_X_SPACES(i))
                    .where(column(C.YEAR).isEqualTo(bindMarker(C.YEAR)),
                            column(C.MONTH).isEqualTo(bindMarker(C.MONTH)))
                    .build()
            psRoomsByYearAndMonth.put(i, cqlSession.prepare(psI))

        }


        psInsertRoomAvailability = cqlSession.prepare(insertTmp.build())

//        psInsertRoomAvailability = cqlSession.prepare(QueryBuilder.insertInto(keyspaceName, C.TABLE_ROOMS)
//                .value(C.YEAR, bindMarker(C.YEAR))
//                .value(C.MONTH, bindMarker(C.MONTH))
//
//                .build()
//        )


//        psInsertReservationByHotelDate = cqlSession.prepare(QueryBuilder.insertInto(keyspaceName, TABLE_RESERVATION_BY_HOTEL_DATE)
//                .value(HOTEL_ID, bindMarker(HOTEL_ID))
//                .value(START_DATE, bindMarker(START_DATE))
//                .value(END_DATE, bindMarker(END_DATE))
//                .value(ROOM_NUMBER, bindMarker(ROOM_NUMBER))
//                .value(CONFIRMATION_NUMBER, bindMarker(CONFIRMATION_NUMBER))
//                .value(GUEST_ID, bindMarker(GUEST_ID))
//                .build());


        /*if (psExistReservation == null) {
            psExistReservation = cqlSession.prepare(selectFrom(keyspaceName, TABLE_RESERVATION_BY_CONFI).column(CONFIRMATION_NUMBER)
                    .where(column(CONFIRMATION_NUMBER).isEqualTo(bindMarker(CONFIRMATION_NUMBER)))
                    .build());
            psFindReservation = cqlSession.prepare(
                    selectFrom(keyspaceName, TABLE_RESERVATION_BY_CONFI).all()
                            .where(column(CONFIRMATION_NUMBER).isEqualTo(bindMarker(CONFIRMATION_NUMBER)))
                            .build());
            psSearchReservation = cqlSession.prepare(
                    selectFrom(keyspaceName, TABLE_RESERVATION_BY_HOTEL_DATE).all()
                            .where(column(HOTEL_ID).isEqualTo(bindMarker(HOTEL_ID)))
                            .where(column(START_DATE).isEqualTo(bindMarker(START_DATE)))
                            .build());
            psDeleteReservationByConfirmation = cqlSession.prepare(
                    deleteFrom(keyspaceName, TABLE_RESERVATION_BY_CONFI)
                            .where(column(CONFIRMATION_NUMBER).isEqualTo(bindMarker(CONFIRMATION_NUMBER)))
                            .build());
            psDeleteReservationByHotelDate = cqlSession.prepare(
                    deleteFrom(keyspaceName, TABLE_RESERVATION_BY_HOTEL_DATE)
                            .where(column(HOTEL_ID).isEqualTo(bindMarker(HOTEL_ID)))
                            .where(column(START_DATE).isEqualTo(bindMarker(START_DATE)))
                            .where(column(ROOM_NUMBER).isEqualTo(bindMarker(ROOM_NUMBER)))
                            .build());
            psInsertReservationByHotelDate = cqlSession.prepare(QueryBuilder.insertInto(keyspaceName, TABLE_RESERVATION_BY_HOTEL_DATE)
                    .value(HOTEL_ID, bindMarker(HOTEL_ID))
                    .value(START_DATE, bindMarker(START_DATE))
                    .value(END_DATE, bindMarker(END_DATE))
                    .value(ROOM_NUMBER, bindMarker(ROOM_NUMBER))
                    .value(CONFIRMATION_NUMBER, bindMarker(CONFIRMATION_NUMBER))
                    .value(GUEST_ID, bindMarker(GUEST_ID))
                    .build());
            psInsertReservationByConfirmation = cqlSession.prepare(QueryBuilder.insertInto(keyspaceName, TABLE_RESERVATION_BY_CONFI)
                    .value(CONFIRMATION_NUMBER, bindMarker(CONFIRMATION_NUMBER))
                    .value(HOTEL_ID, bindMarker(HOTEL_ID))
                    .value(START_DATE, bindMarker(START_DATE))
                    .value(END_DATE, bindMarker(END_DATE))
                    .value(ROOM_NUMBER, bindMarker(ROOM_NUMBER))
                    .value(GUEST_ID, bindMarker(GUEST_ID))
                    .build());
            logger.info("Statements have been successfully prepared.");*/

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
                .withPartitionKey(C.MONTH, DataTypes.INT)
        // add columns
        for (i in minSizeOfRoom..maxSizeOfRoom)
            baseTable = baseTable.withColumn(C.ROOM_WITH_X_SPACES(i), DataTypes.setOf(DataTypes.INT))

        cqlSession.execute(baseTable.build())
        logger.debug("+ Table '{}' has been created (if needed)", C.TABLE_ROOMS.asInternal())
    }

    fun insertData(roomData: List<RoomData>) {
//        psInsertRoomAvailability.bind()
        val stms = roomData.map { prepareRoomData(it) }.toList()
        cqlSession.execute(BatchStatement.builder(DefaultBatchType.LOGGED)
                .addStatements(stms)
                .build())
    }

    private fun prepareRoomData(data: RoomData): BoundStatement {
//        var tmp = psInsertRoomAvailability.bind(data.year, data.month)
        var tmp = psInsertRoomAvailability.boundStatementBuilder()
        tmp.setInt(C.YEAR, data.year)
        tmp.setInt(C.MONTH, data.month)
        for (i in minSizeOfRoom..maxSizeOfRoom) {
            tmp.setSet(C.ROOM_WITH_X_SPACES(i), data.rooms.get(i).orEmpty(), Int::class.javaObjectType)
        }
        return tmp.build()
    }


//    Objects.requireNonNull(reservation);
//        if (null == reservation.getConfirmationNumber()) {
//            // Generating a new reservation number if none has been provided
//            reservation.setConfirmationNumber(UUID.randomUUID().toString());
//        }
//        // Insert into 'reservations_by_hotel_date'
//        BoundStatement bsInsertReservationByHotel =
//                psInsertReservationByHotelDate.bind(reservation.getHotelId(), reservation.getStartDate(),
//                        reservation.getEndDate(), reservation.getRoomNumber(), reservation.getConfirmationNumber(),
//                        reservation.getGuestId());
//        // Insert into 'reservations_by_confirmation'
//        BoundStatement bsInsertReservationByConfirmation =
//                psInsertReservationByConfirmation.bind(reservation.getConfirmationNumber(), reservation.getHotelId(),
//                        reservation.getStartDate(), reservation.getEndDate(), reservation.getRoomNumber(),
//                        reservation.getGuestId());
//        BatchStatement batchInsertReservation = BatchStatement
//                    .builder(DefaultBatchType.LOGGED)
//                    .addStatement(bsInsertReservationByHotel)
//                    .addStatement(bsInsertReservationByConfirmation)
//                    .build();
//        cqlSession.execute(batchInsertReservation);
//        return reservation.getConfirmationNumber();

    private fun createReservationTables() {

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
