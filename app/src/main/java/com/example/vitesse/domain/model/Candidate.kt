package com.example.vitesse.domain.model

import com.example.vitesse.data.entity.CandidateDto
import java.time.LocalDateTime

/**
 * Data class representing a candidate.
 *
 * @param id the id of the candidate. Generated automatically
 * @param firstName the firstname of the candidate
 * @param surName the surname of the candidate
 * @param phoneNumbers the phone numbers of the candidate
 * @param email the email of the candidate
 * @param dateOfBirth the date of birth of the candidate
 * @param expectedSalaryEuros the expected salary in euros of the candidate
 * @param note the note regarding the candidate
 * @param profilePicture the profile picture of the candidate
 */
data class Candidate(
    val id: Long,
    var firstName: String,
    var surName: String,
    val phoneNumbers: String,
    val email: String,
    val dateOfBirth: LocalDateTime,
    val expectedSalaryEuros: Int,
    val note: String,
    val profilePicture: String,
    val favorite : Boolean
) {
    /**
     * Creates a CandidateDto object from a Candidate object.
     */
    fun toDto(): CandidateDto {
        return CandidateDto(
            id = this.id,
            firstName = this.firstName,
            surName = this.surName,
            phoneNumbers = this.phoneNumbers,
            email = this.email,
            dateOfBirth = this.dateOfBirth,
            expectedSalaryEuros = expectedSalaryEuros,
            note = this.note,
            profilePicture = this.profilePicture,
            favorite = this.favorite

        )
    }

    companion object {
        /**
         * Creates a Candidate object from a CandidateDto object.
         *
         * @param candidateDto The candidateDto object to convert.
         * @return A Candidate object with data mapped from the CandidateDto object.
         */
        fun fromDto(candidateDto: CandidateDto): Candidate {
            return Candidate(
                id = candidateDto.id,
                firstName = candidateDto.firstName,
                surName = candidateDto.surName,
                phoneNumbers = candidateDto.phoneNumbers,
                email = candidateDto.email,
                dateOfBirth = candidateDto.dateOfBirth,
                expectedSalaryEuros = candidateDto.expectedSalaryEuros,
                note = candidateDto.note,
                profilePicture = candidateDto.profilePicture,
                favorite = candidateDto.favorite
            )
        }
    }
}
