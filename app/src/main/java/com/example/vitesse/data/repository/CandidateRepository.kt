package com.example.vitesse.data.repository

import com.example.vitesse.data.dao.CandidateDtoDao
import com.example.vitesse.data.entity.CandidateDto
import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import com.example.vitesse.domain.model.Candidate
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * The repository class for managing candidate data.
 *
 * @param candidateDao The data access object for ExerciseDto.
 */
class CandidateRepository @Inject constructor(private val candidateDao: CandidateDtoDao) :
    CandidateRepositoryInterface {

    /**
     * Retrieves all candidates from the database.
     *
     * @return the list of all candidates.
     */
    override suspend fun getAllCandidate(): List<Candidate> {
        return candidateDao.getAllCandidate()
            .first()
            .map { candidateDto ->
                Candidate.fromDto(candidateDto)
            }
    }

    /**
     * Retrieves a candidate from the database based on the specified ID.
     *
     * @param id the ID of the candidate to fetch.
     * @return the user object if found, or null if no user exists with the given ID.
     */
    override suspend fun getCandidateById(id: Long): Candidate? {
        return candidateDao.getCandidateById(id)?.let { candidateDto ->
            Candidate.fromDto(candidateDto)
        }
    }


    /**
     * Adds a new Candidate to the database.
     *
     * @param candidate the Candidate to be added.
     */
    override suspend fun addCandidate(candidate: Candidate) {
        candidateDao.insertCandidate(candidate.toDto())
    }

    /**
     * Deletes an candidate from the database.
     *
     * @param candidate The candidate to be deleted.
     */
    override suspend fun deleteCandidate(candidate: Candidate) {
        candidate.id?.let {
            candidateDao.deleteCandidateById(
                id = candidate.id
            )
        }
    }

    /**
     * Updates a candidate in the database.
     *
     * @param candidate the candidate object with updated data.
     */
    override suspend fun updateCandidate(candidate: CandidateDto) {
        candidateDao.updateCandidate(candidate)
    }
}