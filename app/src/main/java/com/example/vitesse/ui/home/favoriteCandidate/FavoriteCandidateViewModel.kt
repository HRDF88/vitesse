package com.example.vitesse.ui.home.favoriteCandidate

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.domain.usecase.candidate.AddCandidateToFavoriteUseCase
import com.example.vitesse.domain.usecase.candidate.DeleteCandidateToFavoriteUseCase
import com.example.vitesse.domain.usecase.candidate.GetFavoriteCandidateUseCase
import com.example.vitesse.ui.home.candidate.CandidateUiState
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
class FavoriteCandidateViewModel @Inject constructor(
    private val addCandidateToFavoriteUseCase: AddCandidateToFavoriteUseCase,
    private val deleteCandidateToFavoriteUseCase: DeleteCandidateToFavoriteUseCase,
    private val getFavoriteCandidateUseCase: GetFavoriteCandidateUseCase
) : ViewModel() {
    /**
     * The state flow to all candidates.
     */
    private val _favoriteCandidateFlow = MutableStateFlow<List<Candidate>>(emptyList())
    val favoriteCandidateFlow : StateFlow<List<Candidate>> = _favoriteCandidateFlow.asStateFlow()

    /**
     * Ui State of candidate data
     */
    private val _uiState = MutableStateFlow(FavoriteCandidateUiState())
    val uiState : StateFlow<FavoriteCandidateUiState> = _uiState.asStateFlow()

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
     * Loads all favorite candidates using the getFavoriteUseCase and updates the favoriteCandidateFlow.
     */
    suspend fun loadFavoriteCandidate(){
        try{
            val favoriteCandidate = getFavoriteCandidateUseCase.execute()
            _favoriteCandidateFlow.value= favoriteCandidate
        }catch (e:Exception){
            val errorMessage=(R.string.error_load_favorite_candidate).toString()
            onError(errorMessage)
        }
    }
    /**
     * Adds a new favorite candidate using the addCandidateToFavoriteUseCase and reload all favorite candidates.
     *
     * @param candidate the new favorite candidate to be added.
     */
    suspend fun addNewFavoriteCandidate(candidate: Candidate){
        try {
            addCandidateToFavoriteUseCase.execute(candidate)
            loadFavoriteCandidate()
        }catch (e:Exception){
            val errorMessage = (R.string.error_add_favorite_candidate).toString()
            onError(errorMessage)
        }
    }

    /**
     *Deletes a favorite candidate using the deleteCandidateToFavoriteUseCase and reload all favorite candidates.
     *
     * @param candidate the favorite candidate to delete.
     */
    suspend fun deleteFavoriteCandidate(candidate: Candidate){
        try {
            deleteCandidateToFavoriteUseCase.execute(candidate)
            loadFavoriteCandidate()
        } catch (e:Exception){
            val errorMessage = (R.string.error_delete_favorite_candidate).toString()
            onError(errorMessage)
        }
    }
}