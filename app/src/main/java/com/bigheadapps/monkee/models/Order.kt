package com.bigheadapps.monkee.models

import com.google.firebase.Timestamp

data class Order(
    var product: String = "",
    var productName: String = "",
    val quantity: Int = 0,
    val sellerName: String = "",
    val sellerId: String = "",
    val paidAt: Timestamp = Timestamp.now(),
    val totalCost: Double = 0.0,
    val delivery: Double = 0.0,
    val image: String = "",
    val hasPaidForOrder: Boolean = false,
    val customerBillingAddress: String = "",
    val customerCountry: String = "",
    val customerPhoneNumber: String = "",
    val customerName: String = "",
    val customerId: String = "",
    val customerEmail: String = "",
    val merchantCut: Double = 0.0,
    val dispatcherCut: Double = 0.0
)