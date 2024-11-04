package com.example.vitesse.data.webService

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Object to use retrofit library and moshi json parser.
 */
object RetrofitClient {
    private const val baseUrl = "https://continentl.com/"
    private val login = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val apiKeyInterceptor = Interceptor { chain ->
        val url = chain.request().url.newBuilder()
            .addQueryParameter("key", "sk-ek3E67288afd60804445")
            .build()
        val request = chain.request().newBuilder().url(url).build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(login)
        .addInterceptor(apiKeyInterceptor)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val currencyChangeApiService: CurrencyChangeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
            .create(CurrencyChangeApiService::class.java)
    }
}