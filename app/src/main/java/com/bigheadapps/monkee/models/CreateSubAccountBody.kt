package com.bigheadapps.monkee.models

data class CreateSubAccountBody(
    val account_number: String,
    val business_name: String,
    val business_mobile: String,
    val business_email: String,
    val account_bank: String = "044",
    val country: String = "NG",
    val split_type: String = "percentage",
    val split_value: Double = 0.5
)