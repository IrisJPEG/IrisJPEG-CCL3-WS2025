package com.example.ccl3_ws2025_mindflow.di

import android.app.Application

class MoodyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
