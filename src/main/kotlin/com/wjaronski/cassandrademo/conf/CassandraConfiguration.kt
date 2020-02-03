package com.wjaronski.cassandrademo.conf


import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createKeyspace
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder.dropKeyspace
import com.wjaronski.cassandrademo.repository.ReservationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetSocketAddress


@Configuration
class CassandraConfiguration {
    @Value("\${cassandra.contactPoint:127.0.0.1}")
    protected var cassandraHost: String = "127.0.0.1"

    @Value("\${cassandra.port:9042}")
    protected var cassandraPort: Int = 9042

    @Value("\${cassandra.localDataCenterName:datacenter1}")
    protected var localDataCenterName: String = "datacenter1"

    @Value("\${cassandra.keyspaceName:reservation}")
    var keyspaceName: String = "reservation"

    // Option to drop schema and generate table again at startup
    @Value("\${cassandra.dropSchema:true}")
    var dropSchema: Boolean = false


    private val logger = LoggerFactory.getLogger(ReservationRepository::class.java!!)

    @Bean
    fun keyspace(): CqlIdentifier {
        return CqlIdentifier.fromCql(keyspaceName)
    }

    @Bean
    fun cqlSession(): CqlSession {
        logger.info("Creating Keyspace and expected table in Cassandra if not present.")
        CqlSession.builder()
                .addContactPoint(InetSocketAddress(cassandraHost, cassandraPort))
                .withLocalDatacenter(localDataCenterName)
                .build().use { tmpSession ->
                    if (dropSchema) {
                        tmpSession.execute(dropKeyspace(keyspace()).ifExists().build())
                        logger.debug("+ Keyspace '{}' has been dropped (if existed)", keyspace())
                    }
                    tmpSession.execute(createKeyspace(keyspace()).ifNotExists().withSimpleStrategy(1).build())
                    logger.debug("+ Keyspace '{}' has been created (if needed)", keyspace())
                }
        return CqlSession.builder()
                .addContactPoint(InetSocketAddress(cassandraHost, cassandraPort))
                .withKeyspace(keyspace())
                .withLocalDatacenter(localDataCenterName)
                .build()
    }
}
