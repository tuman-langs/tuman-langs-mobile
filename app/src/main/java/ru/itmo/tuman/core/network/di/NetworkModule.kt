package ru.itmo.tuman.core.network.di

import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.itmo.tuman.BuildConfig
import ru.itmo.tuman.core.network.AndroidInternetAvailability
import ru.itmo.tuman.core.network.InternetAvailability
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

val networkModule = module {
    single<Json> { Json { ignoreUnknownKeys = true } }
    single {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.CATALOG_BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single<InternetAvailability> { AndroidInternetAvailability(androidContext()) }
}
