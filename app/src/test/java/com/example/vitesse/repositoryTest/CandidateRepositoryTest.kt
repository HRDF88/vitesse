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
        //Initializing mocks before use
        MockitoAnnotations.openMocks(this)

        //Initializing the repository with the CandidateDtoDao mock
        candidateRepository = CandidateRepository(candidateDtoDao)
    }

    // Testing the getAllCandidate method
    @Test
    fun `test getAllCandidate should return list of candidates`() = runTest {
        //Creation of fictitious data for candidates
        val candidateList = listOf(
            CandidateDto(
                1L,
                "John",
                "Doe",
                "123456789",
                "john.doe@example.com",
                testDate,
                50000,
                "Good",
                null,
                false
            ),
            CandidateDto(
                2L,
                "Jane",
                "Smith",
                "987654321",
                "jane.smith@example.com",
                testDate,
                60000,
                "Excellent",
                null,
                true
            )
        )

        //Simulation of the return of the DAO with flowOf
        Mockito.`when`(candidateDtoDao.getAllCandidate()).thenReturn(flowOf(candidateList))

        //Calling the method to test
        val result = candidateRepository.getAllCandidate()

        //Assertions to check results
        assertEquals(2, result.size) // Vérifier que deux candidats sont retournés
        assertEquals("John", result[0].firstName) // Vérifier le prénom du premier candidat
        assertEquals("Doe", result[0].surName)  // Vérifier le nom du premier candidat
        assertEquals("Jane", result[1].firstName) // Vérifier le prénom du deuxième candidat
        assertEquals("Smith", result[1].surName) // Vérifier le nom du deuxième candidat
    }


    // Testing the getCandidateById method
    @Test
    fun `test getCandidateById should return a specific candidate`() = runTest {
        val candidate = Candidate(
            1L,
            "John",
            "Doe",
            "123456789",
            "john.doe@example.com",
            testDate,
            50000,
            "Good",
            null,
            false
        )

        // Simulate the return of the DAO for a specific candidate
        Mockito.`when`(candidateDtoDao.getCandidateById(1L)).thenReturn(flowOf(candidate.toDto()))

        val result = candidateRepository.getCandidateById(1L)

        // Check results
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

        // Call method to add to favorites
        candidateRepository.addCandidateToFavorite(candidate)

        // Check that the DAO method was called with the candidate ID
        Mockito.verify(candidateDtoDao).addCandidateToFavorite(candidate.id)
    }


    // Test to delete a candidate
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

        //Calling the method to delete a candidate
        candidateRepository.deleteCandidate(candidate)

        //Check that the DAO method has been called
        Mockito.verify(candidateDtoDao).deleteCandidateById(candidateId)
    }
}