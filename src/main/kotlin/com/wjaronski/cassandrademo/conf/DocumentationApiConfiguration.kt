package com.wjaronski.cassandrademo.conf

import com.wjaronski.cassandrademo.CassandraDemoApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2


@Configuration
@EnableSwagger2
class DocumentationApiConfiguration {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .groupName("ReservationService")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.wjaronski.cassandrademo.controller"))
                .paths(PathSelectors.regex("/api/v1.*"))
                .build()
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
    }

    /**
     * Initialization of documentation
     *
     * @return static infos
     */
    private fun apiInfo(): ApiInfo {
        val builder = ApiInfoBuilder()
        builder.title("Reservation Service RESTful API")
        builder.description("Provides the ability to create, read, update and delete reservations.")
        builder.version(CassandraDemoApplication::class.java.getPackage().implementationVersion)
        return builder.build()
    }
}
