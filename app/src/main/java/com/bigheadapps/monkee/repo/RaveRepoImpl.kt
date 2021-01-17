package com.bigheadapps.monkee.repo

import com.bigheadapps.monkee.helpers.Resource
import com.bigheadapps.monkee.models.CreateSubAccountBody
import com.bigheadapps.monkee.models.SubAccountResult
import com.example.cleannews.retrofit.RaveService

class RaveRepoImpl(private val raveService: RaveService) : RaveRepo {
    override suspend fun createSubAccount(
        accountNumber: String,
        businessName: String,
        businessMobile: String,
        businessEmail: String
    ): Resource<SubAccountResult> {
        try {
            val body = CreateSubAccountBody(
                account_number = accountNumber,
                business_name = businessName,
                business_mobile = businessMobile,
                business_email = businessEmail
            )

            val result = raveService.createSubAccount(body)

            if (result.isSuccessful) {
                result.body()?.let {
                    return Resource.success(result.body())
                } ?: return Resource.error("Failed to create sub account", null)
            } else {
                return Resource.error("Unsuccessful: ${result.message()}", null)
            }
        } catch (e: Exception) {
            return Resource.error("Failed to create sub account: ${e.message}", null)
        }
    }

    override suspend fun deleteSubAccount(id: String): Resource<SubAccountResult> {
        try {
            val result = raveService.deleteSubAccount(id = id)

            if (result.isSuccessful) {
                result.body()?.let {
                    return Resource.success(result.body())
                } ?: return Resource.error("Failed to delete sub account", null)
            } else {
                return Resource.error("Unsuccessful: ${result}", null)
            }
        } catch (e: Exception) {
            return Resource.error("Failed to delete sub account: ${e.message.toString()}", null)
        }
    }
}