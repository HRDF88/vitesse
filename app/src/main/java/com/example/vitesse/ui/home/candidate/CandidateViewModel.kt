package com.example.vitesse.ui.home.candidate

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.domain.usecase.AddCandidateUseCase
import com.example.vitesse.domain.usecase.DeleteCandidateUseCase
import com.example.vitesse.domain.usecase.GetAllCandidateUseCase
import com.example.vitesse.domain.usecase.GetCandidateByIdUseCase
import com.example.vitesse.domain.usecase.UpdateCandidateUseCase
import com.example.vitesse.ui.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The view model class for managing candidates.
 */
@HiltViewModel
class CandidateViewModel @Inject constructor(
    private val getAllCandidateUseCase: GetAllCandidateUseCase,
    private val resourceProvider: ResourceProvider
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

    private fun onError(errorMessage: String) {
        Log.e(TAG, errorMessage)
        _uiState.update { currentState ->
            currentState.copy(
                error = errorMessage,
                isLoading = false // stop loading on error
            )
        }
    }

    /**
     * Update error state to reset its value after the error message is broadcast.
     */
    fun updateErrorState() {
        _uiState.update { currentState ->
            currentState.copy(error = "")
        }
    }

    /**
     * Fetch all candidates.
     */
    fun getAllCandidates() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = true) // Start loading
        }

        viewModelScope.launch {
            try {
                val candidates = getAllCandidateUseCase.execute()
                _candidateFlow.value = candidates
                _uiState.update { currentState ->
                    currentState.copy(
                        candidates = candidates,
                        isLoading = false // Stop loading when data is fetched
                    )
                }
            } catch (e: Exception) {
                onError(resourceProvider.getString(R.string.error_load_candidate))
            }
        }
    }

}
