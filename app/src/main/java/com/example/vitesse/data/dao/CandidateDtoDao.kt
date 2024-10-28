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
    @Query(value = "SELECT * FROM candidate")
    suspend fun getAllCandidate(): Flow<List<CandidateDto>>

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
    suspend fun getCandidateById(id: Long): CandidateDto?

    /**
     * Updates a candidate in the database.
     *
     * @param candidate the candidate to update.
     */
    @Update
    suspend fun updateCandidate(candidate: CandidateDto)
}


