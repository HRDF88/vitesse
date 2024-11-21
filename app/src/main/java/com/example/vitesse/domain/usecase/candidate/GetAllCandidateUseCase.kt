package com.example.vitesse.domain.usecase

import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import com.example.vitesse.domain.model.Candidate
import javax.inject.Inject

/**
 * This use case class is responsible for retrieving all Candidates.
 */
class GetAllCandidateUseCase @Inject constructor(private val candidateRepositoryInterface: CandidateRepositoryInterface) {
    /**
     * Executes the use case to retrieve all candidates.
     */
    suspend fun execute(): List<Candidate> {
        return candidateRepositoryInterface.getAllCandidate()
    }
}