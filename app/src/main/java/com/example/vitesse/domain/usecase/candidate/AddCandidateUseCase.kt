package com.example.vitesse.domain.usecase

import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import com.example.vitesse.domain.model.Candidate
import javax.inject.Inject

/**
 * This use case class is responsible for adding a new candidate.
 */
class AddCandidateUseCase @Inject constructor(private val candidateRepositoryInterface: CandidateRepositoryInterface) {
    /**
     * Executes the use case to add a new candidate
     *
     * @param candidate The Candidate object to be added.
     */
    suspend fun execute(candidate: Candidate){
        candidateRepositoryInterface.addCandidate(candidate)
    }
}