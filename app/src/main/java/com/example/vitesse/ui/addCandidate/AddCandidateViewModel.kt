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

    // Using StateFlow to handle validation errors
    private val _fieldErrors = MutableStateFlow<Map<String, String?>>(
        mapOf() //Initialize with empty state
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


    /**
     * Validates the provided first name.
     *
     * @param firstName The first name to validate.
     * Adds an error message to the field errors map if the first name is blank.
     */
    fun validateFirstName(firstName: String) {
        val errors = _fieldErrors.value.toMutableMap()
        errors["firstName"] =
            if (firstName.isBlank()) (R.string.field_error_firstname).toString() else null
        _fieldErrors.value = errors
    }

    /**
     * Validates the provided surname.
     *
     * @param surname The surname to validate.
     * Adds an error message to the field errors map if the surname is blank.
     */
    fun validateSurname(surname: String) {
        val errors = _fieldErrors.value.toMutableMap()
        errors["surname"] =
            if (surname.isBlank()) (R.string.field_error_surname).toString() else null
        _fieldErrors.value = errors
    }

    /**
     * Validates the provided phone number.
     *
     * @param phone The phone number to validate.
     * Adds an error message to the field errors map if the phone number is blank.
     */
    fun validatePhone(phone: String) {
        val errors = _fieldErrors.value.toMutableMap()
        errors["phone"] = if (phone.isBlank()) (R.string.field_error_phone).toString() else null
        _fieldErrors.value = errors
    }

    /**
     * Validates the provided email address.
     *
     * @param email The email address to validate.
     * Adds an error message to the field errors map if the email is blank or invalid.
     */
    fun validateEmail(email: String) {
        val errors = _fieldErrors.value.toMutableMap()
        when {
            email.isBlank() -> errors["email"] = (R.string.field_error_email).toString()
            !isEmailValid(email) -> errors["email"] = (R.string.error_format_email).toString()
            else -> errors["email"] = null
        }
        _fieldErrors.value = errors
    }

    /**
     * Checks whether the provided email address is valid.
     *
     * @param email The email address to check.
     * @return True if the email is valid, false otherwise.
     */
    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates the provided date of birth.
     *
     * @param dateOfBirth The date of birth to validate, formatted as a string.
     * Adds an error message to the field errors map if the date is blank or invalid.
     */
    fun validateDateOfBirth(dateOfBirth: String?) {
        val errors = _fieldErrors.value.toMutableMap()

        //Check if date is empty
        errors["dateOfBirth"] = when {
            dateOfBirth.isNullOrBlank() -> (R.string.field_error_date).toString()
            !isDateValid(dateOfBirth) -> (R.string.error_format_date).toString()
            else -> null
        }

        _fieldErrors.value = errors
    }

    /**
     * Checks whether the provided date string is valid.
     *
     * @param date The date string to check, formatted as "dd/MM/yyyy".
     * @return True if the date is valid, false otherwise.
     */
    fun isDateValid(date: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(date, formatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    /**
     * Formats a given LocalDate to a string in the "yyyy-MM-dd" format.
     *
     * @param date The LocalDate to format.
     * @return The formatted date string, or null if the input date is null.
     */
    fun formatDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    /**
     * Parses a date string to a LocalDateTime object.
     *
     * @param dateStr The date string to parse, formatted as "yyyy-MM-dd".
     * @return The corresponding LocalDateTime object, or null if parsing fails.
     */
    fun parseDateToLocalDateTime(dateStr: String?): LocalDateTime? {
        return try {
            // Correct conversion to LocalDateTime using ISO format
            val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            date.atStartOfDay() // Return LocalDateTime from LocalDate
        } catch (e: Exception) {
            // If an exception is thrown, it means the format is invalid
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
                onError((R.string.field_error_firstname).toString())
                false
            }

            candidate.surName.isBlank() -> {
                onError((R.string.field_error_surname).toString())
                false
            }

            candidate.phoneNumbers.isBlank() -> {
                onError((R.string.field_error_phone).toString())
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
                onError((R.string.error_add_candidate.toString()))
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
                // Use the captured image if it exists, otherwise use the candidate's existing image
                val candidateWithImage = candidate.copy(
                    profilePicture = _imageCaptureState.value ?: candidate.profilePicture
                )

                updateCandidateUseCase.execute(candidateWithImage.toDto())
                _uiState.update { it.copy(addResult = true) }
                Log.d("AddCandidateViewModel", "Candidate updated successfully")
            } catch (e: Exception) {
                Log.e("AddCandidateViewModel", "Error updating candidate", e)
                onError((R.string.error_update_candidate).toString())
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
                // If the candidate has a profile image, save it in the ViewModel state
                candidate?.profilePicture?.let {
                    _imageCaptureState.value = it
                }
                Log.d("AddCandidateViewModel", "Candidate loaded: $candidate")
            } catch (e: Exception) {
                Log.e("AddCandidateViewModel", "Error loading candidate", e)
                onError((R.string.error_load_candidate).toString())
            }
        }
    }
}

