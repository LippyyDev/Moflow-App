// di/AppModule.kt
package com.example.moflow.di

import android.content.Context
import androidx.room.Room
import com.example.moflow.data.local.dao.TransactionDao
import com.example.moflow.data.local.database.MoFlowDatabase
import com.example.moflow.data.remote.api.ExchangeRateApi
import com.example.moflow.data.remote.service.ExchangeRateService
import com.example.moflow.data.repository.CurrencyRepositoryImpl
import com.example.moflow.data.repository.TransactionRepositoryImpl
import com.example.moflow.domain.repository.CurrencyRepository
import com.example.moflow.domain.repository.TransactionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoFlowDatabase(@ApplicationContext context: Context): MoFlowDatabase {
        return Room.databaseBuilder(
            context,
            MoFlowDatabase::class.java,
            "moflow_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: MoFlowDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository {
        return TransactionRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideExchangeRateApi(moshi: Moshi, okHttpClient: OkHttpClient): ExchangeRateApi {
        return Retrofit.Builder()
            .baseUrl("https://v6.exchangerate-api.com/v6/11bee36f695e034d5325ee7e/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ExchangeRateApi::class.java)
    }

    @Provides
    @Singleton
    fun provideExchangeRateService(api: ExchangeRateApi): ExchangeRateService {
        return ExchangeRateService(api)
    }

    @Provides
    @Singleton
    fun provideCurrencyRepository(service: ExchangeRateService): CurrencyRepository {
        return CurrencyRepositoryImpl(service)
    }
}