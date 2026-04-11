package com.utilityhub.mfexplorer.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.utilityhub.mfexplorer.data.local.MFDatabase
import com.utilityhub.mfexplorer.data.local.dao.ExploreCacheDao
import com.utilityhub.mfexplorer.data.local.dao.WatchlistDao
import com.utilityhub.mfexplorer.data.remote.api.MfApiService
import com.utilityhub.mfexplorer.data.repository.MfRepositoryImpl
import com.utilityhub.mfexplorer.data.repository.WatchlistRepositoryImpl
import com.utilityhub.mfexplorer.domain.repository.MfRepository
import com.utilityhub.mfexplorer.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.mfapi.in/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideMfApiService(retrofit: Retrofit): MfApiService =
        retrofit.create(MfApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MFDatabase =
        Room.databaseBuilder(context, MFDatabase::class.java, "mf_explorer.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWatchlistDao(db: MFDatabase): WatchlistDao = db.watchlistDao()

    @Provides
    fun provideExploreCacheDao(db: MFDatabase): ExploreCacheDao = db.exploreCacheDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMfRepository(impl: MfRepositoryImpl): MfRepository

    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(impl: WatchlistRepositoryImpl): WatchlistRepository
}