package com.wjaronski.cassandrademo.conf


import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createKeyspace
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder.dropKeyspace
import com.wjaronski.cassandrademo.conf.logging.LoggerDelegate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetSocketAddress


@Configuration
class CassandraConfiguration(
        val p: AppSettings
) {

    private val logger by LoggerDelegate()

    @Bean
    fun keyspace(): CqlIdentifier {
        return CqlIdentifier.fromCql(p.cass.keyspaceName)
    }

    @Bean
    fun cqlSession(): CqlSession {
        logger.info("Creating Keyspace and expected table in Cassandra if not present.")
        CqlSession.builder()
                .addContactPoint(InetSocketAddress(p.cass.contactPoint, p.cass.port))
                .withLocalDatacenter(p.cass.localDataCenterName)
                .build().use { tmpSession ->
                    if (p.cass.dropSchema) {
                        tmpSession.execute(dropKeyspace(keyspace()).ifExists().build())
                        logger.debug("+ Keyspace '{}' has been dropped (if existed)", keyspace())
                    }
                    tmpSession.execute(createKeyspace(keyspace()).ifNotExists().withSimpleStrategy(3).build())
                    logger.debug("+ Keyspace '{}' has been created (if needed)", keyspace())
                }
        return CqlSession.builder()
                .addContactPoint(InetSocketAddress(p.cass.contactPoint, p.cass.port))
                .withKeyspace(keyspace())
                .withLocalDatacenter(p.cass.localDataCenterName)
                .build()
    }

    //    @PreDestroy
    fun cleanupSession(cqlSession: CqlSession) {
        if (!cqlSession.isClosed) {
            cqlSession.close()
            logger.info("+ CqlSession has been closed")
        }
    }
}
