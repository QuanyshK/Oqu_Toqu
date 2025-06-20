package com.example.oqutoqu


import android.app.Application
import com.example.oqutoqu.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
            networkModule,
            authModule,
            profileModule,
            scienceModule,
                chatModule,
                scholarModule
            )
        }
    }
}
