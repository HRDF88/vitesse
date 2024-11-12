package com.example.vitesse.ui.addCandidate

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.domain.usecase.AddCandidateUseCase
import com.example.vitesse.domain.usecase.GetCandidateByIdUseCase
import com.example.vitesse.domain.usecase.UpdateCandidateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCandidateViewModel @Inject constructor(
    private val getCandidateByIdUseCase: GetCandidateByIdUseCase,
    private val addCandidateUseCase: AddCandidateUseCase,
    private val updateCandidateUseCase: UpdateCandidateUseCase
) : ViewModel() {

    /**
     * The state flow to all candidates.
     */
    private val _candidateFlow = MutableStateFlow<Candidate?>(null)
    val candidateFlow: StateFlow<Candidate?> = _candidateFlow.asStateFlow()

    /**
     * Ui State of candidate data
     */
    private val _uiState = MutableStateFlow(AddCandidateUiState())
    val uiState: StateFlow<AddCandidateUiState> = _uiState.asStateFlow()

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

    /**
     * Charger les informations du candidat par ID.
     * @param candidateId L'identifiant du candidat.
     */
    fun loadCandidateById(candidateId: Long) {
        viewModelScope.launch {
            try {
                val candidate = getCandidateByIdUseCase.execute(candidateId)
                _candidateFlow.value = candidate
            } catch (e: Exception) {
                onError((R.string.error_load_candidate).toString())
            }
        }
    }

    /**
     * Adds a new candidate to the database using the provided use case.
     * The UI state is updated based on whether the operation was successful or not.
     *
     * @param candidate The candidate data to be added.
     */
    fun addCandidate(candidate: Candidate) {
        viewModelScope.launch {
            try {
                addCandidateUseCase.execute(candidate)
                _uiState.value=AddCandidateUiState(error = "", addResult = true)
                Log.d("AddCandidateViewModel", "ajout du candidat" )

            } catch (e: Exception) {
                onError((R.string.error_add_candidate).toString())
                Log.e("AddCandidateViewModel", "Erreur lors de l'ajout du candidat", e)

            }
        }
    }

    /**
     * Updates the data of an existing candidate using the provided use case.
     * The candidate is converted to a `CandidateDto` before being passed to the use case.
     * The UI state is updated based on whether the update was successful or not.
     *
     * @param candidate The candidate data to be updated.
     */
    fun updateCandidate(candidate: Candidate) {
        viewModelScope.launch {
            try {
                // Call the update use case
                updateCandidateUseCase.execute(candidate.toDto())  // Convert to CandidateDto before passing
                _uiState.value = AddCandidateUiState(addResult = true)  // Success, update the UI state
            } catch (e: Exception) {
                onError((R.string.error_update_candidate).toString())
            }
        }
    }
}