package com.spartanai.spartanaimedia

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import com.spartanai.spartanaimedia.di.appModule
import net.sqlcipher.database.SQLiteDatabase
import okhttp3.OkHttpClient
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MediaApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        // Global Exception Handler for debugging
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("SpartanAI_CRASH", "FATAL CRASH on thread ${thread.name}: ${throwable.message}", throwable)
            // Log full stack trace for forensic analysis
            throwable.printStackTrace()
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
                add(OkHttpNetworkFetcherFactory(get<OkHttpClient>()))
                add(SvgDecoder.Factory())
            }
            .memoryCachePolicy(coil3.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil3.request.CachePolicy.ENABLED)
            .build()
    }
}
