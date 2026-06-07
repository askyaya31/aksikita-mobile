package com.example.prototypevolunteerapp.di

import com.example.prototypevolunteerapp.data.model.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindCandidateRepository(
        impl: CandidateRepository
    ): ICandidateRepository
}