package com.example.vitesse.ui.detailsCandidate

import com.example.vitesse.domain.model.Candidate

data class DetailCandidateUiState(
    val candidate: Candidate? = null,  // To store candidate details.
    val isLoading: Boolean = false,
    val error: String = "",
    val isDeleted: Boolean = false
)