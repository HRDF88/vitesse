package com.example.vitesse.domain.usecase.candidate

import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import com.example.vitesse.domain.model.Candidate
import javax.inject.Inject

/**
 * This use case class is responsible for retrieving all favorites Candidates.
 */
class GetFavoriteCandidateUseCase @Inject constructor(private val candidateRepositoryInterface: CandidateRepositoryInterface) {
    /**
     * Executes the use case to retrieve all favorites candidates.
     */
    suspend fun execute(): List<Candidate> {
        return candidateRepositoryInterface.getFavoriteCandidate()
    }
}