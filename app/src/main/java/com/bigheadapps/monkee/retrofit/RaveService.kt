package com.example.cleannews.retrofit

import com.bigheadapps.monkee.models.CreateSubAccountBody
import com.bigheadapps.monkee.models.SubAccountResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Query

interface RaveService {

    @POST("subaccounts")
    suspend fun createSubAccount(
        @Body subAccountBody: CreateSubAccountBody
    ): Response<SubAccountResult>

    @DELETE("subaccount/{id}")
    suspend fun deleteSubAccount(
        @Query("id") id: String
    ): Response<SubAccountResult>

}