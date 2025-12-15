package com.foliolib.app.core.di

import com.foliolib.app.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleBooksRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenLibraryRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsbnDbRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @GoogleBooksRetrofit
    fun provideGoogleBooksRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @OpenLibraryRetrofit
    fun provideOpenLibraryRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://openlibrary.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @IsbnDbRetrofit
    fun provideIsbnDbRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api2.isbndb.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // API Interfaces
    @Provides
    @Singleton
    fun provideGoogleBooksApi(@GoogleBooksRetrofit retrofit: Retrofit): com.foliolib.app.data.remote.api.GoogleBooksApi {
        return retrofit.create(com.foliolib.app.data.remote.api.GoogleBooksApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenLibraryApi(@OpenLibraryRetrofit retrofit: Retrofit): com.foliolib.app.data.remote.api.OpenLibraryApi {
        return retrofit.create(com.foliolib.app.data.remote.api.OpenLibraryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIsbnDbApi(@IsbnDbRetrofit retrofit: Retrofit): com.foliolib.app.data.remote.api.IsbnDbApi {
        return retrofit.create(com.foliolib.app.data.remote.api.IsbnDbApi::class.java)
    }
}
