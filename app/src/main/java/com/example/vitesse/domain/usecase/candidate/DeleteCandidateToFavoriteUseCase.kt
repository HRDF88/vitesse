package com.example.vitesse.domain.usecase.candidate

import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import com.example.vitesse.domain.model.Candidate
import javax.inject.Inject

/**
 * This use case class is responsible for deleting a favorite candidate.
 */
class DeleteCandidateToFavoriteUseCase @Inject constructor(private val candidateRepositoryInterface: CandidateRepositoryInterface) {
    /**
     * Executes the use case to delete a favorite candidate.
     *
     * @param candidate The favorite Candidate object to be deleted.
     */
    suspend fun execute(candidate: Candidate) {
        candidateRepositoryInterface.deleteCandidateToFavorite(candidate)
    }
}