package com.example.vitesse.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.vitesse.data.entity.CandidateDto
import kotlinx.coroutines.flow.Flow

@Dao
interface CandidateDtoDao {

    /**
     * Inserts a candidate into the database.
     *
     * @param candidate the candidate to insert.
     * @return the ID of the newly inserted candidate.
     */
    @Insert
    suspend fun insertCandidate(candidate: CandidateDto): Long

    /**
     * Retrieves all candidate from the database
     *
     * @return A flow of a list of CandidateDto objects representing all candidate.
     */
    @Query(value = "SELECT * FROM candidate ORDER BY surName, firstName")
    fun getAllCandidate(): Flow<List<CandidateDto>>

    /**
     * Deletes a candidate from the database by ID.
     *
     * @param id the ID of the user to delete.
     */
    @Query(value = "DELETE FROM candidate WHERE id=:id")
    suspend fun deleteCandidateById(id: Long)

    /**
     * Retrieves a candidate from the database by ID.
     *
     * @param id the ID of the candidate to retrieve.
     * @return the CandidateDto object representing the candidate, or null if not found.
     */
    @Query(value = "SELECT*FROM candidate WHERE id=:id")
    fun getCandidateById(id: Long): Flow<CandidateDto?>

    /**
     * Updates a candidate in the database.
     *
     * @param candidate the candidate to update.
     */
    @Update
    suspend fun updateCandidate(candidate: CandidateDto): Int

    /**
     * Retrieves a favorite candidate from the database.
     *
     * @return A flow of a list of CandidateDto objects representing all favorites candidates.
     */
    @Query(value = "SELECT* FROM candidate WHERE favorite=1")
    fun getFavoriteCandidate(): Flow<List<CandidateDto>>

    /**
     * Adds a candidate to the list of favorites.
     *
     * @param id The ID of the candidate to add to favorites.
     */
    @Query(value = "UPDATE candidate SET favorite=1 WHERE id =:id")
    suspend fun addCandidateToFavorite(id: Long)

    /**
     * Deletes a candidate from the list of favorites.
     *
     * @param id The ID of the candidate to remove from favorites.
     */
    @Query(value = "UPDATE candidate SET favorite=0 WHERE id =:id")
    suspend fun deleteCandidateToFavorite(id: Long)
}


