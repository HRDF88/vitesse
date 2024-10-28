package com.example.vitesse.domain.usecase

import com.example.vitesse.data.repositoryInterfaces.CandidateRepositoryInterface
import com.example.vitesse.domain.model.Candidate
import javax.inject.Inject

/**
 * This use case class is responsible for retrieving a candidate by Id.
 */
class GetCandidateByIdUseCase @Inject constructor(private val candidateRepositoryInterface: CandidateRepositoryInterface){
    /**
     * Executes the use case to retrieve a candidate by ID.
     */
    suspend fun execute(id:Long):Candidate?{
        return candidateRepositoryInterface.getCandidateById(id)
    }
}