package com.example.vitesse.domain.usecase

import com.example.vitesse.data.entity.CandidateDto
import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import javax.inject.Inject

/**
 * This use case class is responsible for updating a candidate.
 */
class UpdateCandidateUseCase @Inject constructor(private val candidateRepositoryInterface: CandidateRepositoryInterface) {
    /**
     * Executes the use case to update a candidate.
     */
    suspend fun execute(candidate: CandidateDto) {
        candidateRepositoryInterface.updateCandidate(candidate)
    }
}