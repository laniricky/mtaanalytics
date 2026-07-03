package com.mtaanimation.growthos.android.di

import com.mtaanimation.growthos.android.data.datastore.AuthDataStore
import com.mtaanimation.growthos.android.data.network.AuthApiService
import com.mtaanimation.growthos.android.data.network.DashboardApiService
import com.mtaanimation.growthos.android.domain.repository.DashboardRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(authDataStore: AuthDataStore): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    @Provides
    @Singleton
    fun provideAuthApiService(client: HttpClient, authDataStore: AuthDataStore): AuthApiService =
        AuthApiService(client, authDataStore)

    @Provides
    @Singleton
    fun provideDashboardApiService(client: HttpClient, authDataStore: AuthDataStore): DashboardApiService =
        DashboardApiService(client, authDataStore)

    @Provides
    @Singleton
    fun provideDashboardRepository(apiService: DashboardApiService): DashboardRepository =
        DashboardRepository(apiService)

    @Provides
    @Singleton
    fun provideStatsApiService(client: HttpClient, authDataStore: AuthDataStore): com.mtaanimation.growthos.android.data.network.StatsApiService =
        com.mtaanimation.growthos.android.data.network.StatsApiService(client, authDataStore)

    @Provides
    @Singleton
    fun provideStatsRepository(apiService: com.mtaanimation.growthos.android.data.network.StatsApiService): com.mtaanimation.growthos.android.domain.repository.StatsRepository =
        com.mtaanimation.growthos.android.domain.repository.StatsRepository(apiService)
}
