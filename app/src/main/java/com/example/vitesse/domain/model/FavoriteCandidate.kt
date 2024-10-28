package com.example.vitesse.domain.model

import com.example.vitesse.data.entity.FavoriteCandidateDto

/**
 * Data class representing a FavoriteCandidate.
 *
 * @param id the id of the candidate. Generated automatically
 * @param idCandidate the id of the user add in favorite
 */
data class FavoriteCandidate(
    val id: Long,
    val idCandidate: Long
) {
    /**
     * Creates a FavoriteCandidateDto object from a FavoriteCandidate object.
     */
    fun toDto(): FavoriteCandidateDto {
        return FavoriteCandidateDto(
            id = this.id,
            idCandidate = this.idCandidate
        )
    }

    companion object {
        /**
         * Creates a FavoriteCandidate object from a favoriteCandidateDto object.
         *
         * @param favoriteCandidateDto The favoriteCandidateDto object to convert.
         * @return A FavoriteCandidate object with data mapped from the FavoriteCandidateDto object.
         */
        fun fromDto(favoriteCandidateDto: FavoriteCandidateDto): FavoriteCandidate {
            return FavoriteCandidate(
                id = favoriteCandidateDto.id,
                idCandidate = favoriteCandidateDto.idCandidate
            )
        }
    }
}