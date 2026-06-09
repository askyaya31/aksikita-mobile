package com.example.prototypevolunteerapp.di

import com.example.prototypevolunteerapp.core.AppConfig
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val localhostFixInterceptor = Interceptor { chain ->
            val response    = chain.proceed(chain.request())
            val contentType = response.body?.contentType()
            if (contentType?.subtype != "json") {
                return@Interceptor response
            }

            val bodyString = response.body?.string() ?: return@Interceptor response

            val fixedBody = bodyString
                .replace("http:\\/\\/localhost:8000", "http:\\/\\/10.0.2.2:8000")
                .replace("https:\\/\\/localhost:8000", "http:\\/\\/10.0.2.2:8000")
                .replace("http://localhost:8000", "http://10.0.2.2:8000")
                .replace("https://localhost:8000", "http://10.0.2.2:8000")

            response.newBuilder()
                .body(fixedBody.toResponseBody(contentType))
                .build()
        }
        
        val ngrokInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(ngrokInterceptor)
            .addInterceptor(localhostFixInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
