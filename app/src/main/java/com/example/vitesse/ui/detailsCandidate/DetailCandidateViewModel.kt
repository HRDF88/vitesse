package com.example.vitesse.ui.detailsCandidate

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitesse.R
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
import java.io.IOException
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

@HiltViewModel
class DetailCandidateViewModel @Inject constructor(
    private val deleteCandidateUseCase: DeleteCandidateUseCase,
    private val getCandidateByIdUseCase: GetCandidateByIdUseCase,
    private val addCandidateToFavoriteUseCase: AddCandidateToFavoriteUseCase,
    private val deleteCandidateToFavoriteUseCase: DeleteCandidateToFavoriteUseCase,
    private val convertEurosToGbpUseCase: ConvertEurosToGbpUseCase
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
            _uiState.update { currentState -> currentState.copy(isLoading = true) }

            try {
                // Récupérer les détails du candidat
                val candidate = getCandidateByIdUseCase.execute(candidateId)

                if (candidate != null) {
                    val birthDate = candidate.dateOfBirth.toLocalDate()
                    val age = calculateAge(birthDate)

                    // Effectuer la conversion du salaire en GBP
                    val salaryInPounds = try {
                        convertEurosToGbpUseCase(candidate.expectedSalaryEuros.toDouble())
                    } catch (e: Exception) {
                        Log.e("ExpectedSalary", "Erreur de conversion du salaire en GBP", e)
                        null // Si une erreur se produit, le salaire en GBP est null
                        onError((R.string.error_currencyConversion).toString())
                    } catch (e: IOException) {
                        Log.e("ExpectedSalary", "Pas de connexion Internet", e)
                        null // Si une erreur se produit, le salaire en GBP est null
                        onError((R.string.error_io).toString())
                    }

                    // Mettre à jour l'UI avec les nouvelles données (y compris le salaire en GBP)
                    _uiState.update { currentState ->
                        currentState.copy(
                            candidate = candidate,
                            age = age,
                            expectedSalaryPounds = salaryInPounds?.let { "%.2f".format(it) }
                                ?: "Invalid salary",
                            isLoading = false
                        )
                    }
                } else {
                    onError((R.string.candidate_not_found).toString())
                }
            } catch (e: Exception) {
                onError((R.string.error_load_candidate).toString())
                _uiState.update { currentState -> currentState.copy(isLoading = false) }
            }
        }
    }

    /**
     * Calculates the age of a candidate based on their date of birth.
     *
     * @param birthDate The birth date of the candidate as a `LocalDateTime`.
     * @return The calculated age as an integer, representing the difference
     *         between the current year and the year of birth.
     */
    private fun calculateAge(birthDate: LocalDate): Int {
        val today = LocalDate.now()
        val age = Period.between(birthDate, today).years
        Log.d("DetailCandidateViewModel", "Date de naissance: $birthDate, Âge calculé: $age")
        return age
    }

    /**
     * Adds a new favorite candidate using the addCandidateToFavoriteUseCase and reload all favorite candidates.
     *
     * @param candidate the new favorite candidate to be added.
     */
    suspend fun addNewFavoriteCandidate(candidate: Candidate) {
        try {
            addCandidateToFavoriteUseCase.execute(candidate)
        } catch (e: Exception) {
            val errorMessage = (R.string.error_add_favorite_candidate).toString()
            onError(errorMessage)
        }
    }

    /**
     *Deletes a favorite candidate using the deleteCandidateToFavoriteUseCase and reload all favorite candidates.
     *
     * @param candidate the favorite candidate to delete.
     */
    suspend fun deleteFavoriteCandidate(candidate: Candidate) {
        try {
            deleteCandidateToFavoriteUseCase.execute(candidate)
        } catch (e: Exception) {
            val errorMessage = (R.string.error_delete_favorite_candidate).toString()
            onError(errorMessage)
        }
    }

    /**
     * Deletes a candidate using `deleteCandidateUseCase`. Updates the UI state to indicate
     * that the candidate has been deleted.
     *
     * @param candidate The candidate to delete.
     */
    fun deleteCandidate(candidate: Candidate) {
        viewModelScope.launch {
            try {
                deleteCandidateUseCase.execute(candidate)

                // // Update the status to indicate that the candidate has been deleted.
                _uiState.value = _uiState.value.copy(
                    isDeleted = true
                )
            } catch (e: Exception) {
                val errorMessage = (R.string.error_delete_candidate).toString()
                onError(errorMessage)
            }
        }
    }
}