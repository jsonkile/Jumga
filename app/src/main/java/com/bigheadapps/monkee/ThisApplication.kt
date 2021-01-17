package com.bigheadapps.monkee

import android.app.Application
import com.bigheadapps.monkee.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class ThisApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        //Start Timber
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }

        //Start Koin
        startKoin {
            androidLogger()
            androidContext(this@ThisApplication)
            modules(appModule)
        }
    }
}