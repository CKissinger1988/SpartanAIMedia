package com.spartanai.spartanaimedia

import android.app.Application
import com.spartanai.spartanaimedia.di.appModule
import net.sqlcipher.database.SQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MediaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize SQLCipher
        SQLiteDatabase.loadLibs(this)

        startKoin {
            androidLogger()
            androidContext(this@MediaApplication)
            modules(appModule)
        }
    }
}
