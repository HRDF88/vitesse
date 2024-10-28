package com.example.vitesse.domain.usecase.favoriteCandidate

import com.example.vitesse.data.repositoryInterfaces.FavoriteCandidateRepositoryInterface
import com.example.vitesse.domain.model.FavoriteCandidate
import javax.inject.Inject

/**
 * This use case class is responsible for deleting a favorite candidate.
 */
class DeleteFavoriteCandidateUseCase @Inject constructor(private val favoriteCandidateRepositoryInterface: FavoriteCandidateRepositoryInterface) {
    /**
     * Executes the use case to delete a favorite candidate.
     *
     * @param favoriteCandidate The FavoriteCandidate object to be deleted.
     */
    suspend fun execute(favoriteCandidate: FavoriteCandidate) {
        favoriteCandidateRepositoryInterface.deleteFavoriteCandidate(favoriteCandidate)
    }
}