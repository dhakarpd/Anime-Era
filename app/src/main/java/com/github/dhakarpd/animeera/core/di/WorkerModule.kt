package com.github.dhakarpd.animeera.core.di

import androidx.work.WorkerFactory
import com.github.dhakarpd.animeera.core.AppWorkerFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    /**@Binds annotation tells Hilt which implementation to use when it needs to provide an
     * instance of an interface. Here a good choice as App Worker Factory can be just mapped
     * directly with Worker Factory no need to explicitly create object of it**/
    @Binds
    @Singleton
    abstract fun bindWorkerFactory(
        factory: AppWorkerFactory
    ): WorkerFactory
}