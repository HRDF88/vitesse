package com.example.vitesse.domain.usecase.favoriteCandidate

import com.example.vitesse.data.repositoryInterfaces.FavoriteCandidateRepositoryInterface
import com.example.vitesse.domain.model.FavoriteCandidate
import javax.inject.Inject

/**
 * This use case class is responsible for retrieving all Favorite Candidates.
 */
class GetAllFavoriteCandidateUseCase @Inject constructor(private val favoriteCandidateRepositoryInterface: FavoriteCandidateRepositoryInterface){
    /**
     * Executes the use case to retrieve all favorite candidates.
     */
    suspend fun execute():List<FavoriteCandidate>{
        return favoriteCandidateRepositoryInterface.getAllFavoriteCandidate()
    }
}