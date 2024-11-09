package com.example.vitesse.ui.detailsCandidate

import com.example.vitesse.domain.model.Candidate

data class  DetailCandidateUiState (
    val candidate: Candidate? = null,  // Pour stocker les détails du candidat
    val isLoading: Boolean = false,
    val error : String =""
    )