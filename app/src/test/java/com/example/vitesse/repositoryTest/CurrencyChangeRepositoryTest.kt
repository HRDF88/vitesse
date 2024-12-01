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
        // Mock l'API
        currencyChangeRepository = mock(CurrencyChangeRepository::class.java)

    }

    @Test
    fun `should return exchange rate of 0,85 when API responds successfully`() = runBlocking {
        // Créer une réponse valide simulée
        val validRateResponse = 0.85


        // Mock l'appel API pour qu'il retourne la réponse simulée
        `when`(currencyChangeRepository.getExchangeRate()).thenReturn(validRateResponse)

        // Appel de la méthode pour récupérer le taux de change
        val result = currencyChangeRepository.getExchangeRate()

        // Vérifier que le taux de change récupéré est bien de 0.85
        assertEquals(0.85, result, "Le taux de change retourné doit être égal à 0.85")
    }
}