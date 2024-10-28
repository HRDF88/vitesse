package com.example.vitesse.domain.usecase.favoriteCandidate

import com.example.vitesse.data.repositoryInterfaces.FavoriteCandidateRepositoryInterface
import com.example.vitesse.domain.model.FavoriteCandidate
import javax.inject.Inject

/**
 * This use case class is responsible for adding a new favorite candidate.
 */
class AddFavoriteCandidateUseCase @Inject constructor(private val favoriteCandidateRepositoryInterface: FavoriteCandidateRepositoryInterface) {
    /**
     * Executes the use case to add a new favorite candidate
     *
     * @param FavoriteCandidate The FavoriteCandidate object to be added.
     */
    suspend fun execute(favoriteCandidate: FavoriteCandidate) {
        favoriteCandidateRepositoryInterface.addFavoriteCandidate(favoriteCandidate)
    }
}