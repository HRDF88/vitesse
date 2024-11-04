package com.example.vitesse.data.repositoryInterfaces

import com.example.vitesse.data.entity.CandidateDto
import com.example.vitesse.domain.model.Candidate

/**
 * Interface for managing candidate date.
 */
interface CandidateRepositoryInterface {

    /**
     * Gets all candidates.
     *
     * @return the list of Candidate objects.
     */
    suspend fun getAllCandidate(): List<Candidate>

    /**
     * Gets a candidate from the database based on the specified ID
     *
     * @return the candidate object if found, or null if no candidate exists with the given ID.
     */
    suspend fun getCandidateById(id: Long): Candidate?

    /**
     * Adds a candidate.
     *
     * @param candidate The candidate to add.
     */
    suspend fun addCandidate(candidate: Candidate)

    /**
     * Deletes a candidate.
     *
     * @param candidate The candidate to delete.
     */
    suspend fun deleteCandidate(candidate: Candidate)

    /**
     * Update a candidate.
     *
     * @param candidate The candidate to update, represented as a CandidateDto object.
     */
    suspend fun updateCandidate(candidate: CandidateDto)

    /**
     * Gets all favorite candidates.
     *
     * @return the list of favorite candidate Candidate objects.
     */
    suspend fun getFavoriteCandidate(): List<Candidate>

    /**
     * Adds a candidate to the list of favorites.
     *
     * @param candidate The candidate object to add to favorites.
     */
    suspend fun addCandidateToFavorite(candidate: Candidate)

    /**
     * Deletes a candidate to the list of favorites.
     *
     * @param candidate The candidate object to delete to favorites.
     */
    suspend fun deleteCandidateToFavorite(candidate: Candidate)
}