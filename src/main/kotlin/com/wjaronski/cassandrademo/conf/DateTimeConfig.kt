package com.wjaronski.cassandrademo.conf

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
import org.springframework.format.support.DefaultFormattingConversionService
import org.springframework.format.support.FormattingConversionService

import java.time.format.DateTimeFormatter

@Configuration
internal class DateTimeConfig {

    @Bean
    fun conversionService(): FormattingConversionService {
        val conversionService = DefaultFormattingConversionService(false)

        val registrar = DateTimeFormatterRegistrar()
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        registrar.registerFormatters(conversionService)

        // other desired formatters

        return conversionService
    }
}