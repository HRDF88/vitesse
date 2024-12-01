package com.example.vitesse.repositoryTest

import com.example.vitesse.data.dao.CandidateDtoDao
import com.example.vitesse.data.entity.CandidateDto
import com.example.vitesse.data.repository.CandidateRepository
import com.example.vitesse.domain.model.Candidate
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.time.LocalDateTime


class CandidateRepositoryTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var candidateDtoDao: CandidateDtoDao
    private lateinit var candidateRepository: CandidateRepository

    private val testDate = LocalDateTime.of(1990, 1, 1, 10, 0, 0, 0)

    @Before
    fun setUp() {
        // Initialisation des mocks avant l'utilisation
        MockitoAnnotations.openMocks(this)

        // Initialisation du repository avec le mock de CandidateDtoDao
        candidateRepository = CandidateRepository(candidateDtoDao)
    }

    // Test de la méthode getAllCandidate
    @Test
    fun `test getAllCandidate should return list of candidates`() = runTest {
        // Création de données fictives pour les candidats
        val candidateList = listOf(
            CandidateDto(1L, "John", "Doe", "123456789", "john.doe@example.com", testDate, 50000, "Good", null, false),
            CandidateDto(2L, "Jane", "Smith", "987654321", "jane.smith@example.com", testDate, 60000, "Excellent", null, true)
        )

        // Simulation du retour du DAO avec flowOf
        Mockito.`when`(candidateDtoDao.getAllCandidate()).thenReturn(flowOf(candidateList))

        // Appel de la méthode à tester
        val result = candidateRepository.getAllCandidate()

        // Assertions pour vérifier les résultats
        assertEquals(2, result.size) // Vérifier que deux candidats sont retournés
        assertEquals("John", result[0].firstName) // Vérifier le prénom du premier candidat
        assertEquals("Doe", result[0].surName)  // Vérifier le nom du premier candidat
        assertEquals("Jane", result[1].firstName) // Vérifier le prénom du deuxième candidat
        assertEquals("Smith", result[1].surName) // Vérifier le nom du deuxième candidat
    }


    // Test de la méthode getCandidateById
    @Test
    fun `test getCandidateById should return a specific candidate`() = runTest{
        val candidate = Candidate(1L, "John", "Doe", "123456789", "john.doe@example.com", testDate, 50000, "Good", null, false)

        // Simuler le retour du DAO pour un candidat spécifique
        Mockito.`when`(candidateDtoDao.getCandidateById(1L)).thenReturn(flowOf(candidate.toDto()))

        val result = candidateRepository.getCandidateById(1L)

        // Vérifier les résultats
        if (result != null) {
            assertEquals("John", result.firstName)
        }
        if (result != null) {
            assertEquals("Doe", result.surName)
        }
    }

    @Test
    fun `test addCandidateToFavorite should update the favorite status`() = runTest {
        val candidate = Candidate(
            id = 1L,
            firstName = "John",
            surName = "Doe",
            phoneNumbers = "123456789",
            email = "john.doe@example.com",
            dateOfBirth = testDate,
            expectedSalaryEuros = 50000,
            note = "Good",
            profilePicture = null,
            favorite = false
        )

        // Appel de la méthode pour ajouter aux favoris
        candidateRepository.addCandidateToFavorite(candidate)

        // Vérifier que la méthode du DAO a bien été appelée avec l'ID du candidat
        Mockito.verify(candidateDtoDao).addCandidateToFavorite(candidate.id)
    }


    // Test de suppression d'un candidat
    @Test
    fun `test deleteCandidateById should delete the candidate by ID`() = runTest {
        val candidateId = 1L
        val candidate = Candidate(
            id = 1L,
            firstName = "John",
            surName = "Doe",
            phoneNumbers = "123456789",
            email = "john.doe@example.com",
            dateOfBirth = testDate,
            expectedSalaryEuros = 50000,
            note = "Good",
            profilePicture = null,
            favorite = false
        )

        // Appel de la méthode pour supprimer un candidat
        candidateRepository.deleteCandidate(candidate)

        // Vérifier que la méthode du DAO a bien été appelée
        Mockito.verify(candidateDtoDao).deleteCandidateById(candidateId)
    }
}