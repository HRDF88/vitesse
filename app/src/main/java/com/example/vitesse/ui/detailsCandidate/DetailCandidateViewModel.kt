package com.example.vitesse.ui.detailsCandidate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.domain.usecase.DeleteCandidateUseCase
import com.example.vitesse.domain.usecase.GetCandidateByIdUseCase
import com.example.vitesse.domain.usecase.candidate.AddCandidateToFavoriteUseCase
import com.example.vitesse.domain.usecase.candidate.DeleteCandidateToFavoriteUseCase
import com.example.vitesse.domain.usecase.candidate.currencyConversionUseCase.ConvertEurosToGbpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

@HiltViewModel
class DetailCandidateViewModel @Inject constructor(
    private val deleteCandidateUseCase: DeleteCandidateUseCase,
    private val getCandidateByIdUseCase: GetCandidateByIdUseCase,
    private val addCandidateToFavoriteUseCase: AddCandidateToFavoriteUseCase,
    private val deleteCandidateToFavoriteUseCase: DeleteCandidateToFavoriteUseCase,
    private val convertEurosToGbpUseCase: ConvertEurosToGbpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailCandidateUiState())
    val uiState: StateFlow<DetailCandidateUiState> = _uiState.asStateFlow()


    /**
     * Loads a candidate's details by its ID.
     */
    fun loadCandidateDetails(candidateId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val candidate = getCandidateByIdUseCase.execute(candidateId)

                if (candidate != null) {
                    // Calculate age from birth date
                    val birthDate = candidate.dateOfBirth.toLocalDate() // Ensure it's LocalDate
                    val age = calculateAge(birthDate)

                    // Convert salary to GBP
                    val salaryInPounds = try {
                        convertEurosToGbpUseCase(candidate.expectedSalaryEuros.toDouble())
                    } catch (e: Exception) {
                        onError("Error converting salary to GBP")
                        null
                    }

                    // Update the UI state
                    _uiState.update {
                        it.copy(
                            candidate = candidate,
                            age = age, // Pass calculated age
                            expectedSalaryPounds = salaryInPounds?.let { "%.2f".format(it) } ?: "Invalid salary",
                            profilePicture = candidate.profilePicture,
                            isLoading = false
                        )
                    }
                } else {
                    onError("Candidate not found")
                }
            } catch (e: Exception) {
                onError("Error loading candidate details")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    /**
     * Helper function to calculate age from birth date.
     */
    private fun calculateAge(birthDate: LocalDate): Int {
        val today = LocalDate.now()
        return Period.between(birthDate, today).years
    }

    /**
     * Handles errors and updates the UI state accordingly.
     */
    private fun onError(errorMessage: String) {
        _uiState.update { currentState ->
            currentState.copy(
                error = errorMessage
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
     * Adds a candidate to the favorites list.
     */
    suspend fun addNewFavoriteCandidate(candidate: Candidate) {
        try {
            addCandidateToFavoriteUseCase.execute(candidate)
        } catch (e: Exception) {
            onError("Error adding to favorites")
        }
    }

    /**
     * Removes a candidate from the favorites list.
     */
    suspend fun deleteFavoriteCandidate(candidate: Candidate) {
        try {
            deleteCandidateToFavoriteUseCase.execute(candidate)
        } catch (e: Exception) {
            onError("Error deleting from favorites")
        }
    }

    /**
     * Deletes a candidate.
     */
    fun deleteCandidate(candidate: Candidate) {
        viewModelScope.launch {
            try {
                deleteCandidateUseCase.execute(candidate)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                onError("Error deleting candidate")
            }
        }
    }
}
