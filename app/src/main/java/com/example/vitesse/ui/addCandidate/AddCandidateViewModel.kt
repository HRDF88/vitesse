package com.example.vitesse.ui.addCandidate

import android.graphics.Bitmap
import android.util.Log
import android.util.Patterns
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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


    // Utilisation de StateFlow pour gérer les erreurs de validation
    private val _fieldErrors = MutableStateFlow<Map<String, String?>>(
        mapOf() // Initialiser avec un état vide
    )
    val fieldErrors: StateFlow<Map<String, String?>> = _fieldErrors

    /**
     * Handles errors by updating the UI state.
     *
     * @param errorMessage The error message to display.
     */
    private fun onError(errorMessage: String) {
        Log.e("AddCandidateViewModel", errorMessage)
        _uiState.update { currentState -> currentState.copy(error = errorMessage) }
    }

    /**
     * Resets the error state after the error has been processed.
     */
    fun updateErrorState() {
        _uiState.update { currentState -> currentState.copy(error = "") }
    }


    // Validation pour le prénom
    fun validateFirstName(firstName: String) {
        val errors = _fieldErrors.value.toMutableMap()
        errors["firstName"] = if (firstName.isBlank()) "First name is required" else null
        _fieldErrors.value = errors
    }

    // Validation pour le nom
    fun validateSurname(surname: String) {
        val errors = _fieldErrors.value.toMutableMap()
        errors["surname"] = if (surname.isBlank()) "Last name is required" else null
        _fieldErrors.value = errors
    }

    // Validation pour le téléphone
    fun validatePhone(phone: String) {
        val errors = _fieldErrors.value.toMutableMap()
        errors["phone"] = if (phone.isBlank()) "Phone number is required" else null
        _fieldErrors.value = errors
    }

    // Validation pour l'email
    fun validateEmail(email: String) {
        val errors = _fieldErrors.value.toMutableMap()
        when {
            email.isBlank() -> errors["email"] = "Email is required"
            !isEmailValid(email) -> errors["email"] = "Invalid email format"
            else -> errors["email"] = null
        }
        _fieldErrors.value = errors
    }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateDateOfBirth(dateOfBirth: String?) {
        val errors = _fieldErrors.value.toMutableMap()

        // Vérifier si la date est vide
        errors["dateOfBirth"] = when {
            dateOfBirth.isNullOrBlank() -> "Date of birth is required"
            !isDateValid(dateOfBirth) -> "Invalid date format" // Vérifiez si le format de la date est valide
            else -> null
        }

        _fieldErrors.value = errors
    }

    // Fonction utilitaire pour valider la date (si vous voulez valider le format)
     fun isDateValid(date: String): Boolean {
        return try {
            // Essayez de parser la date en utilisant un format spécifique (ex: dd/MM/yyyy)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(date, formatter)
            true // Si la date est valide
        } catch (e: DateTimeParseException) {
            false // Si une exception est levée, la date est invalide
        }
    }

    // Méthode pour formater une date au format "yyyy-MM-dd"
    fun formatDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    // Méthode pour parser la date en LocalDateTime
    fun parseDateToLocalDateTime(dateStr: String?): LocalDateTime? {
        return try {
            // Conversion correcte en LocalDateTime en utilisant le format ISO
            val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            date.atStartOfDay() // Retourne LocalDateTime à partir du LocalDate
        } catch (e: Exception) {
            // Si une exception est levée, cela signifie que le format n'est pas valide
            null
        }
    }

    /**
     * Handles image capture by resizing the image and converting it to a ByteArray.
     *
     * @param bitmap The captured image as a Bitmap.
     */
    fun handleImageCapture(bitmap: Bitmap) {
        viewModelScope.launch {
            val byteArray = resizeAndConvertBitmap(bitmap, 512, 512)
            _imageCaptureState.value = byteArray
            Log.d("AddCandidateViewModel", "Image captured and resized to ByteArray")
        }
    }

    /**
     * Resizes a Bitmap to fit within the given dimensions and converts it to a ByteArray.
     *
     * @param bitmap The original Bitmap to resize and convert.
     * @param maxWidth The maximum width of the resized image.
     * @param maxHeight The maximum height of the resized image.
     * @return The converted ByteArray.
     */
    private fun resizeAndConvertBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): ByteArray {
        val ratio = Math.min(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Validates the candidate data before adding or updating in the database.
     *
     * @param candidate The candidate to validate.
     * @return True if the candidate data is valid, false otherwise.
     */
    private fun validateCandidate(candidate: Candidate): Boolean {
        return when {
            candidate.firstName.isBlank() -> {
                onError("First Name cannot be empty")
                false
            }
            candidate.surName.isBlank() -> {
                onError("Last Name cannot be empty")
                false
            }
            candidate.phoneNumbers.isBlank() -> {
                onError("Phone number cannot be empty")
                false
            }
            else -> true
        }
    }

    /**
     * Adds a new candidate to the database after validation.
     *
     * @param candidate The candidate data to add.
     */
    fun addCandidate(candidate: Candidate) {
        if (!validateCandidate(candidate)) return
        viewModelScope.launch {
            try {
                Log.d("AddCandidateViewModel", "Adding candidate: $candidate")
                addCandidateUseCase.execute(candidate)
                _uiState.update { it.copy(error = "", addResult = true) }
                Log.d("AddCandidateViewModel", "Candidate added successfully")
                getAllCandidateUseCase.execute()
            } catch (e: Exception) {
                Log.e("AddCandidateViewModel", "Error adding candidate", e)
                onError("Failed to add candidate.")
            }
        }
    }

    /**
     * Updates an existing candidate's data in the database after validation.
     *
     * @param candidate The candidate data to update.
     */
    fun updateCandidate(candidate: Candidate) {
        if (!validateCandidate(candidate)) return
        viewModelScope.launch {
            try {
                // Utilisez l'image capturée si elle existe, sinon utilisez l'image existante du candidat
                val candidateWithImage = candidate.copy(
                    profilePicture = _imageCaptureState.value ?: candidate.profilePicture
                )

                updateCandidateUseCase.execute(candidateWithImage.toDto())
                _uiState.update { it.copy(addResult = true) }
                Log.d("AddCandidateViewModel", "Candidate updated successfully")
            } catch (e: Exception) {
                Log.e("AddCandidateViewModel", "Error updating candidate", e)
                onError("Failed to update candidate.")
            }
        }
    }

    /**
     * Loads a candidate's details by ID.
     *
     * @param candidateId The ID of the candidate to load.
     */
    fun loadCandidateById(candidateId: Long) {
        viewModelScope.launch {
            try {
                val candidate = getCandidateByIdUseCase.execute(candidateId)
                _candidateFlow.value = candidate
                // Si le candidat a une image de profil, l'enregistrer dans l'état du ViewModel
                candidate?.profilePicture?.let {
                    _imageCaptureState.value = it
                }
                Log.d("AddCandidateViewModel", "Candidate loaded: $candidate")
            } catch (e: Exception) {
                Log.e("AddCandidateViewModel", "Error loading candidate", e)
                onError("Failed to load candidate.")
            }
        }
    }
}

