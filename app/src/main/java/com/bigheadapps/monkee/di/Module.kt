package com.bigheadapps.monkee.di

import com.bigheadapps.monkee.BuildConfig
import com.bigheadapps.monkee.repo.RaveRepo
import com.bigheadapps.monkee.repo.RaveRepoImpl
import com.bigheadapps.monkee.ui.viewmodels.APIViewModel
import com.example.cleannews.retrofit.RaveService
import com.google.gson.GsonBuilder
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    viewModel { APIViewModel(get(), get()) }

    val okHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(
            Interceptor { chain ->
                val builder = chain.request().newBuilder()
                builder.header("Content-Type", "application/json")
                builder.header("Authorization", "Bearer ${BuildConfig.SECRET_KEY}")
                return@Interceptor chain.proceed(builder.build())
            }
        )
    }.build()

    single {
        Retrofit.Builder()
            .baseUrl("https://api.flutterwave.com/v3/")
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(okHttpClient)
            .build().create(RaveService::class.java)
    }

    single<RaveRepo> { RaveRepoImpl(get()) }

}