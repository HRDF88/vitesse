package com.example.vitesse.ui.home.candidate

import com.example.vitesse.domain.model.Candidate

/**
 * Represents the UI state for displaying a list of candidates.
 * This class holds the list of candidates, loading state, and any error messages
 * encountered while fetching or displaying the candidate data.
 *
 * @property error A string holding any error message encountered during data retrieval or processing. Defaults to an empty string.
 * @property isLoading A boolean flag indicating whether the candidates are currently being loaded. Defaults to false.
 * @property candidates A list of `Candidate` objects representing the candidates to be displayed. Defaults to an empty list if no candidates are available.
 */
data class CandidateUiState(
    val error : String ="",
    val isLoading:Boolean=false,
    val candidates: List<Candidate> = emptyList(),
)
