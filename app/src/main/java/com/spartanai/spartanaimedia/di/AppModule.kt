package com.spartanai.spartanaimedia.di

import androidx.room.Room
import com.spartanai.spartanaimedia.data.local.MediaDatabase
import com.spartanai.spartanaimedia.data.remote.*
import com.spartanai.spartanaimedia.data.repository.MediaRepositoryImpl
import com.spartanai.spartanaimedia.domain.repository.MediaRepository
import com.spartanai.spartanaimedia.domain.usecase.GetHomeScreenDataUseCase
import com.spartanai.spartanaimedia.domain.usecase.GetRecommendationsUseCase
import com.spartanai.spartanaimedia.ui.media.MediaViewModel
import net.sqlcipher.database.SupportFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    single {
        // Secure passphrase derivation (demo implementation)
        val passphrase = "SPARTAN_SECURE_KEY_2026".toByteArray()
        val factory = SupportFactory(passphrase)

        Room.databaseBuilder(
            androidContext(),
            MediaDatabase::class.java,
            "spartan_media_encrypted.db"
        )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration()
        .build()
    }
    
    single { get<MediaDatabase>().mediaDao }
    single { get<MediaDatabase>().userDao }

    singleOf(::MediaDownloadManager)
    singleOf(::MediaScraper)
    singleOf(::PiBlockchainManager)
    singleOf(::PiNodeService)
    singleOf(::UpdateManager)
    singleOf(::PreloadManager)
    singleOf(::SearchSuggestionManager)
    singleOf(::RecommendationEngine)
    singleOf(::P2PManager)
    singleOf(::MediaSyncManager)
    singleOf(::MediaRepositoryImpl) { bind<MediaRepository>() }

    // Use Cases
    singleOf(::GetRecommendationsUseCase)
    singleOf(::GetHomeScreenDataUseCase)

    viewModelOf(::MediaViewModel)
}
