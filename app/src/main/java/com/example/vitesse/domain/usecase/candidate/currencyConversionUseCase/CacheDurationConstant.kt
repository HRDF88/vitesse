package com.example.vitesse.domain.usecase.candidate.currencyConversionUseCase

/**
 * A constant object that defines the duration for cache storage.
 * The cache duration is set to 24 hours.
 *
 * This value is used to determine the lifespan of cached data in the application.
 * The duration is expressed in milliseconds.
 */
object CacheDurationConstant {
    /**
     * The duration for which the cache is valid.
     * This is set to 24 hours, expressed in milliseconds.
     */
    const val DURATION_CACHE = (24 * 60 * 60 * 1000) //24 hours
}