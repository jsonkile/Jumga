package com.bigheadapps.monkee.models

data class Store(
    var name: String = "Merchant",
    var phone: String = "-",
    var address: String = "-",
    var description: String = "-",
    var accountNumber: String = "-",
    var subAccountRef: String = "",
    var subAccountId: String = "",
    var riderName: String = "Dispatch Rider",
    var riderSubAccountId: String = "-",
    var riderEmail: String = "-",
    var hasPaidSetupFee: Boolean = false
)