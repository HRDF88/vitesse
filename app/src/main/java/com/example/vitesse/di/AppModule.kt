package com.example.vitesse.di

import android.content.Context
import com.example.vitesse.data.dao.CandidateDtoDao
import com.example.vitesse.data.database.AppDataBase
import com.example.vitesse.data.repository.CandidateRepository
import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * AppModule for Hilt (Dependency Injection)
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    /**
     * Provides the CoroutineScope instance for managing coroutines.
     *
     * @return The CoroutineScope instance.
     */
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Provides the Application Database instance.
     *
     * @param context The application context.
     * @param coroutineScope The CoroutineScope instance.
     * @return The AppDatabase instance.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope
    ): AppDataBase {
        return AppDataBase.getDatabase(context, coroutineScope)
    }

    /**
     * Provides the CandidateDtoDao instance for accessing candidate data in the AppDatabase.
     *
     * @param appDataBase the AppDataBase instance.
     * @return the CandidateDtoDao instance.
     */
    @Provides
    fun provideCandidateDao(appDataBase: AppDataBase): CandidateDtoDao {
        return appDataBase.candidateDtoDao()
    }


    /**
     * Provides the CandidateRepositoryInterface instance for managing candidate data.
     *
     * @param candidateDtoDao The candidateDtoDao instance.
     * @return The CandidateRepositoryInterface instance.
     */
    @Provides
    @Singleton
    fun provideCandidateRepository(candidateDtoDao: CandidateDtoDao): CandidateRepositoryInterface {
        return CandidateRepository(candidateDtoDao)
    }
}