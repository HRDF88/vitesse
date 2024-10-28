package com.example.vitesse.data.repository

import com.example.vitesse.data.dao.FavoriteCandidateDtoDao
import com.example.vitesse.data.repositoryInterfaces.FavoriteCandidateRepositoryInterface
import com.example.vitesse.domain.model.FavoriteCandidate
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class FavoriteCandidateRepository @Inject constructor(private val favoriteCandidateDao: FavoriteCandidateDtoDao) :
    FavoriteCandidateRepositoryInterface {

    /**
     * Retrieves all favorite candidates from the database.
     *
     * @return the list of all favorite candidates.
     */
    override suspend fun getAllFavoriteCandidate(): List<FavoriteCandidate> {
        return favoriteCandidateDao.getAllFavoriteCandidate()
            .first()
            .map { favoriteCandidateDto ->
                FavoriteCandidate.fromDto(favoriteCandidateDto)
            }
    }

    /**
     * Adds a new favorite candidate to the database.
     *
     * @param favoriteCandidate The Favorite candidate to be added.
     */
    override suspend fun addFavoriteCandidate(favoriteCandidate: FavoriteCandidate) {
        favoriteCandidateDao.insertFavoriteCandidate(favoriteCandidate.toDto())
    }

    /**
     * Deletes an favorite candidate to the database.
     *
     * @param favoriteCandidate the favorite candidate to be deleted.
     */
    override suspend fun deleteFavoriteCandidate(favoriteCandidate: FavoriteCandidate) {
        favoriteCandidate.id?.let {
            favoriteCandidateDao.deleteFavoriteCandidateById(
                id = favoriteCandidate.id
            )
        }
    }

    /**
     * Retrieves a favorite candidate from the database based on the specified ID.
     *
     * @param id the Id of the favorite candidate to fetch.
     * @return the favoriteCandidate object if found, or null if no exist.
     */
    override suspend fun getFavoriteCandidateById(id: Long): FavoriteCandidate? {
        return favoriteCandidateDao.getFavoriteCandidateById(id)?.let { favoriteCandidateDto ->
            FavoriteCandidate.fromDto(favoriteCandidateDto)
        }
    }
}
