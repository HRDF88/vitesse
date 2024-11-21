package com.example.vitesse.ui.detailsCandidate

import com.example.vitesse.domain.model.Candidate

/**
 * Represents the UI state for displaying detailed information about a candidate.
 * This class holds the various details of a candidate, as well as flags indicating the current
 * loading state, error messages, and other relevant information for the UI to render the candidate's data.
 *
 * @property candidate The candidate's details, including personal information. Can be null if no candidate data is available.
 * @property isLoading A boolean flag indicating whether the candidate's details are currently being loaded.
 * @property error A string that holds any error messages encountered during data retrieval or processing.
 * @property isDeleted A boolean flag indicating whether the candidate has been marked as deleted.
 * @property age The candidate's age, represented as an integer. Can be null if the age is unavailable.
 * @property expectedSalaryPounds A string representing the candidate's expected salary in pounds. Default is "Salary unknown" if not specified.
 * @property profilePicture A byte array holding the candidate's profile picture data. Can be null if no profile picture is available.
 */
data class DetailCandidateUiState(
    val candidate: Candidate? = null,  // To store candidate details.
    val isLoading: Boolean = false,
    val error: String = "",
    val isDeleted: Boolean = false,
    val age : Int? = null,
    val expectedSalaryPounds: String = "Salary unknown",
    val profilePicture: ByteArray? = null
)