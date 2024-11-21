package com.example.vitesse.domain.usecase

import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import com.example.vitesse.domain.model.Candidate
import javax.inject.Inject

/**
 * This use case class is responsible for deleting a candidate.
 */
class DeleteCandidateUseCase @Inject constructor(private val candidateRepositoryInterface: CandidateRepositoryInterface) {
    /**
     * Executes the use case to delete a candidate.
     *
     * @param candidate The Candidate object to be deleted.
     */
    suspend fun execute(candidate: Candidate) {
        candidateRepositoryInterface.deleteCandidate(candidate)
    }
}