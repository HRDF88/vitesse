package com.example.vitesse.ui.detailsCandidate

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.domain.model.CandidateWithAge
import com.example.vitesse.domain.usecase.AddCandidateUseCase
import com.example.vitesse.domain.usecase.DeleteCandidateUseCase
import com.example.vitesse.domain.usecase.GetAllCandidateUseCase
import com.example.vitesse.domain.usecase.GetCandidateByIdUseCase
import com.example.vitesse.domain.usecase.UpdateCandidateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class DetailCandidateViewModel @Inject constructor(
    private val addCandidateUseCase: AddCandidateUseCase,
    private val deleteCandidateUseCase: DeleteCandidateUseCase,
    private val getAllCandidateUseCase: GetAllCandidateUseCase,
    private val getCandidateByIdUseCase: GetCandidateByIdUseCase,
    private val updateCandidateUseCase: UpdateCandidateUseCase
) : ViewModel() {

    /**
     * The state flow to all candidates.
     */
    private val _candidateFlow = MutableStateFlow<List<Candidate>>(emptyList())
    val candidateFlow: StateFlow<List<Candidate>> = _candidateFlow.asStateFlow()

    /**
     * Ui State of candidate data
     */
    private val _uiState = MutableStateFlow(DetailCandidateUiState())
    val uiState: StateFlow<DetailCandidateUiState> = _uiState.asStateFlow()

    /**
     * The state flow for all candidates with age.
     */
    val candidatesWithAge: StateFlow<List<CandidateWithAge>> = _candidateFlow
        /**
         * Transform Candidate object to CandidateWithAge object.
         */
        .map { candidates ->
            candidates.map { candidate ->
                val age = calculateAge(candidate.dateOfBirth)
                CandidateWithAge(candidate, age)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


/*
    // StateFlow pour le salaire en livres
    val expectedSalaryPounds: StateFlow<String> = _candidateFlow
        .map { candidate ->
            candidate?.let {
                val salaryInPounds = convertEurosToPounds(it.expectedSalary)
                "%.2f".format(salaryInPounds)
            } ?: "Salaire inconnu"
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, "Salaire inconnu")
*/
    /**
     * update uiState if there is an error.
     */
    private fun onError(errorMessage: String) {
        Log.e(TAG, errorMessage)
        _uiState.update { currentState ->
            currentState.copy(
                error = errorMessage,

                )
        }
    }

    /**
     * Update error state to reset its value after the error message is broadcast.
     */
    fun updateErrorState(errorMessage: String) {
        val currentState = uiState.value
        val updatedState = currentState.copy(error = errorMessage)
        _uiState.value = updatedState
    }

    fun loadCandidateDetails(candidateId: Long) {
        viewModelScope.launch {
            try {
                val candidate = getCandidateByIdUseCase.execute(candidateId)
                if (candidate != null) {
                    _uiState.update { currentState ->
                        currentState.copy(candidate = candidate, error = "")
                    }
                } else {
                    onError("Candidate not found")
                }
            } catch (e: Exception) {
                onError("Error loading candidate details: ${e.message}")
            }
        }
    }

    // Méthode pour calculer l'âge à partir de la date de naissance
    private fun calculateAge(birthDate: LocalDateTime): Int {
        val currentYear = LocalDate.now().year
        val birthYear = birthDate.year
        return currentYear - birthYear
    }
}