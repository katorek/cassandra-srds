package com.wjaronski.cassandrademo

import com.wjaronski.cassandrademo.conf.AppSettings
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(AppSettings::class)
@SpringBootApplication
class CassandraDemoApplication

fun main(args: Array<String>) {
    runApplication<CassandraDemoApplication>(*args)
}
