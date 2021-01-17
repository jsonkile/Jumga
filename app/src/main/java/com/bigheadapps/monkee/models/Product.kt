package com.bigheadapps.monkee.models

data class Product(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var category: Int = 0,
    var price: Int = 0,
    val quantity: Int = 0,
    val picture: String = "",
    val sellerId: String = ""
)