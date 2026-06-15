package com.spartanai.spartanaimedia

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.spartanai.spartanaimedia.di.appModule
import net.sqlcipher.database.SQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MediaApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        // Global Exception Handler for debugging
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("SpartanAI_CRASH", "Uncaught exception on thread ${thread.name}", throwable)
        }

        // Initialize SQLCipher
        SQLiteDatabase.loadLibs(this)

        startKoin {
            androidLogger()
            androidContext(this@MediaApplication)
            modules(appModule)
        }
    }

    override fun newImageLoader(context: android.content.Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}
