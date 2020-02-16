package com.wjaronski.cassandrademo.model.dto

import java.util.*

data class ReservationInfoDto(
        val uuid: UUID,
        val description: String
) {
    companion object {
        fun withoutUUID(description: String): ReservationInfoDto {
            return withUUID(
                    uuid = UUID.randomUUID(),
                    description = description
            )
        }

        fun withUUID(uuid: UUID, description: String): ReservationInfoDto {
            return ReservationInfoDto(
                    uuid = uuid,
                    description = description
            )
        }
    }
}
