package com.example.vitesse.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.vitesse.data.entity.FavoriteCandidateDto
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCandidateDtoDao {

    /**
     * Inserts an favorite candidate entry into the database
     *
     * @param favoriteCandidate the favorite candidate entry to insert, representing a FavoriteCandidateDto object.
     * @return the Id of the inserted favorite candidate entry.
     */
    @Insert
    suspend fun insertFavoriteCandidate(favoriteCandidate: FavoriteCandidateDto): Long

    /**
     * Retrieves all favorite candidate entries from the database.
     *
     * @return a Flow emitting a list of all favoriteCandidate entries.
     */
    @Query(value = "SELECT * FROM favoritecandidate")
    suspend fun getAllFavoriteCandidate(): Flow<List<FavoriteCandidateDto>>

    /**
     * Delete an favorite Candidate entry from the database by its ID.
     *
     * @param id the ID of the favorite candidate entry to delete.
     */
    @Query(value = "DELETE FROM favoritecandidate WHERE id = :id")
    suspend fun deleteFavoriteCandidateById(id: Long)

    /**
     * Retrieves a favorite candidate from the database by ID.
     *
     * @return the FavoriteCandidateDto object representing the favorite candidate, or null if not fount
     */
    @Query(value = "SELECT*FROM favoritecandidate WHERE id=:id")
    suspend fun getFavoriteCandidateById(id: Long): FavoriteCandidateDto?


}