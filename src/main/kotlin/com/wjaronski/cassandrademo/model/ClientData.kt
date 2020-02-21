package com.wjaronski.cassandrademo.model

import java.util.*

data class ClientData(
        val startDate: Date,
        val endDate: Date,
        val roomSize: Int,
        var call: Runnable? = null
)