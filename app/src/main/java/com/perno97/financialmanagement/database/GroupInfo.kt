package com.perno97.financialmanagement.database

import java.time.LocalDate

data class GroupInfo(
    /*
    Using string because when grouping I want the group date to be the start of the group period
    and not the date of the first movement in the period.
    Could have been done maybe with UNIXEPOCH() but it's not recognized by Room library.
     */
    val groupDate: String,
    val positive: Float,
    val negative: Float
)
