package com.example.vitesse.data.repositoryInterfaces

import com.example.vitesse.domain.model.FavoriteCandidate

/**
 * Interface for managing favorite candidate date.
 */
interface FavoriteCandidateRepositoryInterface {


    /**
     * Gets all favorite candidates.
     *
     * @return the list of FavoriteCandidate objects.
     */
    suspend fun getAllFavoriteCandidate():List<FavoriteCandidate>

    /**
     * Adds a candidate.
     *
     * @param favoriteCandidate The candidate to add.
     */
    suspend fun addFavoriteCandidate(favoriteCandidate: FavoriteCandidate)

    /**
     * Deletes a favorite candidate.
     *
     * @param favoriteCandidate The favorite candidate to delete.
     */
    suspend fun deleteFavoriteCandidate(favoriteCandidate: FavoriteCandidate)


    /**
     * Gets a favorite candidate from the database based on the specified ID
     *
     * @return the favoriteCandidate object if found, or null if no favorite candidate exists with the given ID.
     */

    suspend fun getFavoriteCandidateById(id:Long): FavoriteCandidate?
}