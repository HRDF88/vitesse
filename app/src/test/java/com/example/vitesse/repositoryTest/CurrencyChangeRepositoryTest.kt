package com.example.vitesse.repositoryTest

import com.example.vitesse.data.repository.CurrencyChangeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

class CurrencyChangeRepositoryTest {

    private lateinit var currencyChangeRepository: CurrencyChangeRepository

    @Before
    fun setUp() {
        // Mock API
        currencyChangeRepository = mock(CurrencyChangeRepository::class.java)

    }

    @Test
    fun `should return exchange rate of 0,85 when API responds successfully`() = runBlocking {

        val validRateResponse = 0.85

        // Mock the API call so that it returns the mock response
        `when`(currencyChangeRepository.getExchangeRate()).thenReturn(validRateResponse)

        // Call the method to retrieve the exchange rate
        val result = currencyChangeRepository.getExchangeRate()

        // Check that the recovered exchange rate is indeed 0.85
        assertEquals(0.85, result, "The returned exchange rate must be equal to 0.85")
    }

    @Test
    fun `should accept when API response is 0`() = runBlocking {

        val inValidRateResponse = 0.00
        `when`(currencyChangeRepository.getExchangeRate()).thenReturn(inValidRateResponse)

        val result = currencyChangeRepository.getExchangeRate()

        assertEquals(0.00, result, 0.00)
    }

}