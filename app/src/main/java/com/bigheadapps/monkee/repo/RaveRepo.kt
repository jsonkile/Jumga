package com.bigheadapps.monkee.repo

import com.bigheadapps.monkee.helpers.Resource
import com.bigheadapps.monkee.models.SubAccountResult

interface RaveRepo {
    suspend fun createSubAccount(
        accountNumber: String,
        businessName: String,
        businessMobile: String,
        businessEmail: String
    ): Resource<SubAccountResult>

    suspend fun deleteSubAccount(id: String): Resource<SubAccountResult>
}