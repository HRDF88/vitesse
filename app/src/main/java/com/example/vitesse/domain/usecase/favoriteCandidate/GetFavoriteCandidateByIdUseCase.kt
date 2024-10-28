package com.example.vitesse.domain.usecase.favoriteCandidate

import com.example.vitesse.data.repositoryInterfaces.FavoriteCandidateRepositoryInterface
import com.example.vitesse.domain.model.FavoriteCandidate
import javax.inject.Inject

/**
 * This use case class is responsible for retrieving a favorite candidate by Id.
 */
class GetFavoriteCandidateByIdUseCase @Inject constructor(private val favoriteCandidateRepositoryInterface: FavoriteCandidateRepositoryInterface) {
    /**
     * Executes the use case to retrieve a favorite candidate by ID.
     */
    suspend fun execute(id: Long): FavoriteCandidate? {
        return favoriteCandidateRepositoryInterface.getFavoriteCandidateById(id)
    }
}