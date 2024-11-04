package com.example.vitesse.domain.usecase.candidate

import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import com.example.vitesse.domain.model.Candidate
import javax.inject.Inject

/**
 * This use case class is responsible for adding a new favorite candidate.
 */
class AddCandidateToFavoriteUseCase @Inject constructor(private val candidateRepositoryInterface: CandidateRepositoryInterface) {
    /**
     * Executes the use case to add a new favorite candidate
     *
     * @param candidate The favorite Candidate object to be added.
     */
    suspend fun execute(candidate: Candidate) {
        candidateRepositoryInterface.addCandidateToFavorite(candidate)

    }
}