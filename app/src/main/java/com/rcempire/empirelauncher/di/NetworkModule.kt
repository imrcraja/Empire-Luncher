package com.rcempire.empirelauncher.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.rcempire.empirelauncher.data.network.ModrinthApi
import com.rcempire.empirelauncher.data.network.MojangApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Provides @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { it.proceed(it.request().newBuilder()
            .header("User-Agent", "RCEmpire/EmpireLauncher/0.1.0").build()) }
        .build()

    private fun retrofit(base: String, c: OkHttpClient) = Retrofit.Builder()
        .baseUrl(base).client(c)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()

    @Provides @Singleton fun mojang(c: OkHttpClient): MojangApi =
        retrofit("https://piston-meta.mojang.com/", c).create(MojangApi::class.java)

    @Provides @Singleton fun modrinth(c: OkHttpClient): ModrinthApi =
        retrofit("https://api.modrinth.com/", c).create(ModrinthApi::class.java)
}
