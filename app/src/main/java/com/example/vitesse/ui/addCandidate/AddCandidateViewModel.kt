package com.example.vitesse.ui.addCandidate

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.domain.usecase.AddCandidateUseCase
import com.example.vitesse.domain.usecase.GetAllCandidateUseCase
import com.example.vitesse.domain.usecase.GetCandidateByIdUseCase
import com.example.vitesse.domain.usecase.UpdateCandidateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class AddCandidateViewModel @Inject constructor(
    private val getCandidateByIdUseCase: GetCandidateByIdUseCase,
    private val addCandidateUseCase: AddCandidateUseCase,
    private val updateCandidateUseCase: UpdateCandidateUseCase,
    private val getAllCandidateUseCase: GetAllCandidateUseCase
) : ViewModel() {

    private val _candidateFlow = MutableStateFlow<Candidate?>(null)
    val candidateFlow: StateFlow<Candidate?> = _candidateFlow.asStateFlow()

    private val _uiState = MutableStateFlow(AddCandidateUiState())
    val uiState: StateFlow<AddCandidateUiState> = _uiState.asStateFlow()

    private val _imageCaptureState = MutableStateFlow<ByteArray?>(null)
    val imageCaptureState: StateFlow<ByteArray?> = _imageCaptureState.asStateFlow()

    /**
     * Updates the UI state if there is an error.
     *
     * @param errorMessage The error message to display.
     */
    private fun onError(errorMessage: String) {
        Log.e("AddCandidateViewModel", errorMessage)
        _uiState.update { currentState -> currentState.copy(error = errorMessage) }
    }

    /**
     * Update error state to reset its value after the error message is broadcast.
     */
    fun updateErrorState() {
        _uiState.update { currentState -> currentState.copy(error = "") }
    }

    /**
     * Handles image capture by converting a Bitmap to a ByteArray encoded in Base64.
     *
     * @param bitmap The captured image as a Bitmap.
     */
    fun handleImageCapture(bitmap: Bitmap) {
        viewModelScope.launch {
            val byteArray = convertBitmapToByteArray(bitmap)
            _imageCaptureState.value = byteArray
            Log.d("AddCandidateViewModel", "Image captured and converted to ByteArray")
        }
    }

    /**
     * Saves the captured image in the candidate entity and updates the database.
     *
     * @param candidateId The ID of the candidate to associate the image with.
     */
    fun saveCandidateImage(candidateId: Long) {
        val imageByteArray = _imageCaptureState.value
        if (imageByteArray != null) {
            viewModelScope.launch {
                try {
                    val candidate = getCandidateByIdUseCase.execute(candidateId)
                    if (candidate != null) {
                        val updatedCandidate = candidate.copy(profilePicture = imageByteArray)
                        updateCandidateUseCase.execute(updatedCandidate.toDto())
                        Log.d("AddCandidateViewModel", "Image saved for candidate $candidateId")
                    } else {
                        onError("Candidate not found for ID: $candidateId")
                    }
                } catch (e: Exception) {
                    Log.e("AddCandidateViewModel", "Error saving image for candidate $candidateId", e)
                    onError("Failed to save candidate image.")
                }
            }
        } else {
            onError("No image captured to save.")
        }
    }

    /**
     * Converts a Bitmap to a ByteArray.
     *
     * @param bitmap The Bitmap to convert.
     * @return The converted ByteArray.
     */
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
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
                Log.d("AddCandidateViewModel", "Attempting to add candidate: $candidate")
                addCandidateUseCase.execute(candidate)
                _uiState.value = AddCandidateUiState(error = "", addResult = true)
                Log.d("AddCandidateViewModel", "Candidate added successfully")
                getAllCandidateUseCase.execute()
            } catch (e: Exception) {
                onError((R.string.error_add_candidate).toString())
                Log.e("AddCandidateViewModel", "Error adding candidate", e)
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
                updateCandidateUseCase.execute(candidate.toDto())
                _uiState.value = AddCandidateUiState(addResult = true)
            } catch (e: Exception) {
                onError((R.string.error_update_candidate).toString())
            }
        }
    }

    /**
     * Loads the information of a candidate by ID.
     *
     * @param candidateId The ID of the candidate.
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
}

