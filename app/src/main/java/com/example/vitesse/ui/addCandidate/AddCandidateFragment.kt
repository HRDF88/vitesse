package com.example.vitesse.ui.addCandidate

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.vitesse.R
import com.example.vitesse.databinding.FragmentAddCandidateBinding
import com.example.vitesse.domain.model.Candidate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Fragment to add or edit a candidate.
 * It allows the user to fill out candidate details and either add a new candidate or update an existing one.
 * Uses ViewModel to manage the state and handle the logic.
 *
 * @constructor Creates a new instance of `AddCandidateFragment`.
 */
@AndroidEntryPoint
class AddCandidateFragment : Fragment() {

    private lateinit var binding: FragmentAddCandidateBinding
    private val addCandidateViewModel: AddCandidateViewModel by viewModels()

    private var selectedDate: LocalDate? = null  // Stores the selected birth date

    // Launcher for selecting an image through the content picker
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Handle the selected image URI
                handleImageUri(it)
            }
        }

    /**
     * Called to create the fragment's view. Initializes the binding and loads the candidate data
     * if an ID is provided in the arguments (for editing).
     *
     * @param inflater The LayoutInflater used to inflate the fragment's view.
     * @param container The parent view that this fragment's UI will be attached to.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state.
     * @return The root view of the fragment's layout.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCandidateBinding.inflate(inflater, container, false)

        // Retrieve the candidate ID from arguments to determine if we're adding or editing
        val candidateId = arguments?.getLong("candidateId", -1L) ?: -1L
        if (candidateId != -1L) {
            addCandidateViewModel.loadCandidateById(candidateId) // Load existing candidate data if editing
        }

        return binding.root
    }

    /**
     * Called after the fragment's view has been created.
     * Sets up UI interactions, like setting up the toolbar, observing ViewModel states, and handling user actions.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()  // Initializing the navigation bar
        observeUiState() // Observe UI state for error handling
        observeFieldErrors() // Observe validation errors
        observeCandidateData() // Observe candidate data

        setupListeners()
        updateSaveButtonState()

        binding.addCandidateBirth.setOnClickListener {
            showDatePickerDialog()
        }

        binding.addCandidateSaveButton.setOnClickListener {
            handleSaveOrUpdateCandidate()
        }

        binding.addCandidateProfilePicture.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    override fun onStop() {
        super.onStop()

        // Report that changes have been made
        setFragmentResult("candidate_updated", bundleOf("updated" to true))
    }

    /**
     * Observe validation errors via StateFlow.
     * Collects errors in the StateFlow and updates the UI.
     */
    private fun observeFieldErrors() {
        //Use lifecycleScope to collect emissions from StateFlow
        lifecycleScope.launch {
            addCandidateViewModel.fieldErrors.collect { errors ->
                // Updating errors in UI from fieldErrors
                binding.addCandidateFirstnameLayout.error = errors["firstName"]
                binding.addCandidateSurnameLayout.error = errors["surname"]
                binding.addCandidatePhoneLayout.error = errors["phone"]
                binding.addCandidateMailLayout.error = errors["email"]
                binding.addCandidateBirthLayout.error = errors["dateOfBirth"]
            }
        }
    }

    /**
     * Configures listeners for user input fields to handle validation and state updates.
     * This method sets up real-time and focus-based validation for various fields,
     * ensuring user input is checked as they type or when they finish editing.
     */
    private fun setupListeners() {

        /**
         * Sets up a listener for the first name input field.
         *
         * - Real-time validation: Validates the first name whenever the text changes.
         * - Focus-based validation: Validates the first name when the field loses focus.
         *
         * Behavior:
         * - Updates the save button state after validation.
         * - Ensures the first name is non-blank.
         */
        binding.addCandidateFirstname.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    addCandidateViewModel.validateFirstName(s.toString())
                    updateSaveButtonState()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {  // If the field loses focus
                    addCandidateViewModel.validateFirstName(text.toString()) // Re-validate the field
                }
            }
        }

        /**
         * Sets up a listener for the surname input field.
         *
         * - Real-time validation: Validates the surname whenever the text changes.
         * - Focus-based validation: Validates the surname when the field loses focus.
         * - Auto-formatting: Converts the surname to uppercase as the user types.
         *
         * Behavior:
         * - Updates the save button state after validation.
         * - Ensures the surname is converted to uppercase for consistency.
         */
        binding.addCandidateSurname.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val uppercaseSurname = s.toString().uppercase() // Convert to uppercase
                    if (s.toString() != uppercaseSurname) {
                        setText(uppercaseSurname) // Update text with uppercase version
                        setSelection(uppercaseSurname.length) // Return cursor to end
                    }

                    //Validate surname in uppercase
                    addCandidateViewModel.validateSurname(uppercaseSurname)
                    updateSaveButtonState() // Update save button state
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {  // If the field loses focus
                    val uppercaseSurname =
                        text.toString().uppercase() //Convert to uppercase if necessary
                    setText(uppercaseSurname) // Update text to uppercase
                    setSelection(uppercaseSurname.length) // Return cursor to end
                    addCandidateViewModel.validateSurname(uppercaseSurname) // Re-validate the field
                }
            }
        }

        /**
         * Sets up a listener for the phone number input field.
         *
         * - Real-time validation: Validates the phone number whenever the text changes.
         * - Focus-based validation: Validates the phone number when the field loses focus.
         *
         * Behavior:
         * - Updates the save button state after validation.
         * - Ensures the phone number is non-blank.
         */
        binding.addCandidatePhone.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    addCandidateViewModel.validatePhone(s.toString())
                    updateSaveButtonState()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    addCandidateViewModel.validatePhone(text.toString())
                }
            }
        }

        /**
         * Sets up a listener for the email input field.
         *
         * - Real-time validation: Validates the email whenever the text changes.
         * - Focus-based validation: Validates the email when the field loses focus.
         *
         * Behavior:
         * - Updates the save button state after validation.
         * - Ensures the email is valid and follows the correct format.
         */
        binding.addCandidateMail.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    addCandidateViewModel.validateEmail(s.toString())
                    updateSaveButtonState()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    addCandidateViewModel.validateEmail(text.toString())
                }
            }
        }

        /**
         * Sets up a listener for the date of birth input field.
         *
         * - Real-time validation: Validates the date of birth whenever the text changes.
         * - Focus-based validation: Validates the date of birth when the field loses focus.
         *
         * Behavior:
         * - Updates the save button state after validation.
         * - Ensures the date of birth is valid and non-blank.
         */
        binding.addCandidateBirth.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    addCandidateViewModel.validateDateOfBirth(s.toString())
                    updateSaveButtonState()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    addCandidateViewModel.validateDateOfBirth(text.toString())
                }
            }
        }
    }


    /**
     * Sets up the toolbar for the fragment.
     * This toolbar is used for navigation and showing the correct title (Add/Edit Candidate).
     */
    private fun setupToolbar() {
        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.apply {
            val candidateId = arguments?.getLong("candidateId", -1L) ?: -1L
            title = if (candidateId == -1L) {
                getString(R.string.add_candidate)
            } else {
                getString(R.string.edit_candidate)
            }
            setNavigationIcon(R.drawable.arrow_back) // Set back arrow for navigation
            setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed() // Handle back navigation
            }
        }
    }

    /**
     * Observes the UI state for error messages and displays them in a toast.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            addCandidateViewModel.uiState.collect { uiState ->
                binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                // If there is an error in the UI state, show a toast with the error message
                if (uiState.error.isNotBlank()) {
                    Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                    addCandidateViewModel.updateErrorState() // Reset the error state
                }
            }
        }
    }

    /**
     * Observes the candidate data from the ViewModel and updates the form fields if editing an existing candidate.
     */
    private fun observeCandidateData() {
        viewLifecycleOwner.lifecycleScope.launch {
            addCandidateViewModel.candidateFlow.collect { candidate ->
                candidate?.let {
                    // Pre-fill the form with the candidate's existing data
                    binding.addCandidateFirstname.setText(it.firstName)
                    binding.addCandidateSurname.setText(it.surName)
                    binding.addCandidatePhone.setText(it.phoneNumbers)
                    binding.addCandidateMail.setText(it.email)
                    binding.addCandidateSalary.setText(it.expectedSalaryEuros.toString())
                    binding.addCandidateNote.setText(it.note)

                    // Format the date to dd/MM/yyyy
                    binding.addCandidateBirth.setText(
                        it.dateOfBirth.toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )

                    // Set the selected date for later use
                    selectedDate = it.dateOfBirth.toLocalDate()

                    // Display the profile picture if available
                    it.profilePicture?.let { profilePicture ->
                        if (profilePicture.isNotEmpty()) {
                            val bitmap = BitmapFactory.decodeByteArray(
                                profilePicture,
                                0,
                                profilePicture.size
                            )
                            binding.addCandidateProfilePicture.setImageBitmap(bitmap)
                        }
                    }
                }
            }
        }
    }

    /**
     * Displays a date picker dialog for the user to select a birth date.
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis // Current date in milliseconds

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Set the selected date when the user selects a date
                selectedDate =
                    LocalDate.of(year, month + 1, dayOfMonth) // month is zero-based, hence +1
                // Format the selected date to dd/MM/yyyy
                val formattedDate = selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                binding.addCandidateBirth.setText(formattedDate) // Set the formatted date to the TextInputEditText
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Restrict the date picker to not allow future dates
        datePickerDialog.datePicker.maxDate = today // Set max date as today

        datePickerDialog.show()
    }

    /**
     * Processes the selected image URI.
     * Delegates image resizing and state management to the ViewModel.
     *
     * @param uri The URI of the selected image.
     */
    private fun handleImageUri(uri: Uri) {
        val imageStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(imageStream)

        // Delegate image resizing and state management to the ViewModel
        addCandidateViewModel.handleImageCapture(bitmap)

        // Optionally display the resized image in the UI
        binding.addCandidateProfilePicture.setImageBitmap(bitmap)
    }

    /**
     * Handles saving or updating the candidate based on whether an ID was passed for editing.
     * Validates the input and creates a new candidate object, then calls the appropriate ViewModel method.
     */
    private fun handleSaveOrUpdateCandidate() {
        val firstName = binding.addCandidateFirstname.text.toString()
        val surName = binding.addCandidateSurname.text.toString()
        val phone = binding.addCandidatePhone.text.toString()
        val email = binding.addCandidateMail.text.toString()

        // Validate that all fields are filled
        if (firstName.isBlank() || surName.isBlank() || phone.isBlank() || email.isBlank() || selectedDate == null) {
            Toast.makeText(
                requireContext(),
                (R.string.all_fiel_error).toString(),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Format the date of birth in "yyyy-MM-dd" format via the ViewModel
        val formattedDate = addCandidateViewModel.formatDate(selectedDate)
        if (formattedDate == null) {
            Toast.makeText(requireContext(), (R.string.invalid_date).toString(), Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Parse the date formatted as LocalDateTime via the ViewModel
        val dateOfBirth = addCandidateViewModel.parseDateToLocalDateTime(formattedDate)
        if (dateOfBirth == null) {
            Toast.makeText(
                requireContext(),
                (R.string.error_format_date).toString(),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Get the candidate ID (or 0 if it's an addition)
        val candidateId = arguments?.getLong("candidateId", -1L) ?: -1L

        // If no new image is chosen, use the current (old) image
        val currentProfilePicture = addCandidateViewModel.imageCaptureState.value
            ?: getCurrentProfilePicture()

        // Preserve the "favorite" state of the current candidate if we modify an existing candidate
        val currentFavoriteStatus = addCandidateViewModel.candidateFlow.value?.favorite ?: false

        val candidate = Candidate(
            firstName = firstName,
            surName = surName,
            phoneNumbers = phone,
            email = email,
            dateOfBirth = dateOfBirth,
            expectedSalaryEuros = binding.addCandidateSalary.text.toString().toIntOrNull() ?: 0,
            note = binding.addCandidateNote.text.toString(),
            favorite = currentFavoriteStatus,
            profilePicture = currentProfilePicture,
            id = if (candidateId == -1L) 0 else candidateId
        )
        try {
            Log.d("AddCandidate", "About to update candidate: $candidate")
            if (candidateId == -1L) {
                addCandidateViewModel.addCandidate(candidate)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.success_add_candidate),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                addCandidateViewModel.updateCandidate(candidate)
                Log.d("AddCandidate", "Candidate updated successfully.")
                Toast.makeText(
                    requireContext(),
                    getString(R.string.sucess_update_candidate),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e("AddCandidate", "Error updating candidate: ", e)
            Toast.makeText(
                requireContext(),
                getString(R.string.error_update_candidate),
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


    /**
     * Retrieves the current profile picture from the ViewModel or defaults to a placeholder.
     *
     * @return The profile picture as a byte array, or an empty byte array if no picture is set.
     */
    private fun getCurrentProfilePicture(): ByteArray {
        return addCandidateViewModel.imageCaptureState.value?.takeIf { it.isNotEmpty() }
            ?: byteArrayOf() // Return an empty byte array if no image is selected
    }

    /**
     * Validates all required input fields in the candidate form.
     * This method checks that each field is filled out correctly and that the values meet specific criteria,
     * such as ensuring that the email has a valid format and the date of birth follows a correct format.
     *
     * @return Boolean - Returns `true` if all fields are valid (non-blank, correctly formatted),
     *                   otherwise returns `false`.
     *
     * This method performs the following validations:
     * - First name, surname, phone number, email, and date of birth must not be blank.
     * - The email must be in a valid format (using the `isEmailValid` method).
     * - The date of birth must follow a valid format (using the `isDateValid` method).
     */
    private fun validateAllFields(): Boolean {
        val firstName = binding.addCandidateFirstname.text.toString().trim()
        val surName = binding.addCandidateSurname.text.toString().trim()
        val phone = binding.addCandidatePhone.text.toString().trim()
        val email = binding.addCandidateMail.text.toString().trim()
        val dateOfBirth = binding.addCandidateBirth.text.toString().trim()

        // Check if all fields are filled and valid
        if (firstName.isBlank() || surName.isBlank() || phone.isBlank() || email.isBlank() || dateOfBirth.isBlank()) {
            return false
        }

        //Check if the email is valid
        if (!addCandidateViewModel.isEmailValid(email)) {
            return false
        }

        // Check if the date of birth is valid
        return addCandidateViewModel.isDateValid(dateOfBirth)
    }

    /**
     * Updates the state of the save button based on the validity of all input fields.
     * This method ensures that the save button is enabled only when all fields are valid.
     */
    private fun updateSaveButtonState() {
        val isValid = validateAllFields()
        binding.addCandidateSaveButton.isEnabled = isValid //Adjust the corresponding button
    }

    companion object {
        // This method will create a new instance of the fragment with the provided candidateId argument
        fun newInstance(candidateId: Long): AddCandidateFragment {
            return AddCandidateFragment().apply {
                arguments = Bundle().apply {
                    putLong("candidateId", candidateId) // Pass the candidate ID as an argument
                }
            }
        }
    }
}