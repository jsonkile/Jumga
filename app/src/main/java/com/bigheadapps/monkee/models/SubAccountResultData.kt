package com.bigheadapps.monkee.models

import com.google.gson.annotations.SerializedName

data class SubAccountResultData(
    var id: String,
    @SerializedName("subaccount_id") var subAccountId: String
)