package com.foliolib.app.core.di

import com.foliolib.app.data.repository.BookRepositoryImpl
import com.foliolib.app.data.repository.ReadingRepositoryImpl
import com.foliolib.app.data.repository.StatisticsRepositoryImpl
import com.foliolib.app.data.repository.UserPreferencesRepositoryImpl
import com.foliolib.app.domain.repository.BookRepository
import com.foliolib.app.domain.repository.ReadingRepository
import com.foliolib.app.domain.repository.StatisticsRepository
import com.foliolib.app.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        bookRepositoryImpl: BookRepositoryImpl
    ): BookRepository

    @Binds
    @Singleton
    abstract fun bindReadingRepository(
        readingRepositoryImpl: ReadingRepositoryImpl
    ): ReadingRepository

    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(
        statisticsRepositoryImpl: StatisticsRepositoryImpl
    ): StatisticsRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}
