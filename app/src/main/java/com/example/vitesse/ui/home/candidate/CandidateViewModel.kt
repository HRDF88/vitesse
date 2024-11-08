package com.example.vitesse.ui.home.candidate

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.domain.usecase.AddCandidateUseCase
import com.example.vitesse.domain.usecase.DeleteCandidateUseCase
import com.example.vitesse.domain.usecase.GetAllCandidateUseCase
import com.example.vitesse.domain.usecase.GetCandidateByIdUseCase
import com.example.vitesse.domain.usecase.UpdateCandidateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * The view model class for managing candidates.
 */
@HiltViewModel
class CandidateViewModel @Inject constructor(
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
    private val _uiState = MutableStateFlow(CandidateUiState())
    val uiState: StateFlow<CandidateUiState> = _uiState.asStateFlow()

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
     * Adds a new candidate using the addCandidateUseCase and reload all candidates.
     *
     * @param candidate the new  candidate to be added.
     */
    suspend fun addNewCandidate(candidate: Candidate) {
        try {
            addCandidateUseCase.execute(candidate)
        } catch
            (e: Exception) {
            val errorMessage = (R.string.error_add_favorite_candidate).toString()
            onError(errorMessage)
        }
    }

}