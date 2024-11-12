package com.example.vitesse.ui.home.candidate

import com.example.vitesse.domain.model.Candidate

data class CandidateUiState(
    val error : String ="",
    val isLoading:Boolean=false,
    val candidates: List<Candidate> = emptyList(),
)
