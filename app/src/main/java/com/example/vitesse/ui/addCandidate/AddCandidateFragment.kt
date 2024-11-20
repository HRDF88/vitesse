package com.example.vitesse.ui.addCandidate

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.vitesse.R
import com.example.vitesse.databinding.FragmentAddCandidateBinding
import com.example.vitesse.domain.model.Candidate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

        setupToolbar()  // Initialisation de la barre de navigation
        observeUiState() // Observer l'état UI pour la gestion des erreurs
        observeFieldErrors() // Observer les erreurs de validation
        observeCandidateData() // Observer les données du candidat

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

    /**
     * Observer les erreurs de validation via StateFlow.
     * Collecte les erreurs dans le StateFlow et met à jour l'UI.
     */
    private fun observeFieldErrors() {
        // Utiliser lifecycleScope pour collecter les émissions de StateFlow
        lifecycleScope.launch {
            addCandidateViewModel.fieldErrors.collect { errors ->
                // Mise à jour des erreurs dans l'UI à partir de fieldErrors
                binding.addCandidateFirstnameLayout.error = errors["firstName"]
                binding.addCandidateSurnameLayout.error = errors["surname"]
                binding.addCandidatePhoneLayout.error = errors["phone"]
                binding.addCandidateMailLayout.error = errors["email"]
                binding.addCandidateBirthLayout.error = errors["dateOfBirth"]
            }
        }
    }


    private fun setupListeners() {
        // Listener pour First Name
        binding.addCandidateFirstname.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    addCandidateViewModel.validateFirstName(s.toString())
                    updateSaveButtonState()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {  // Si le champ perd le focus
                    addCandidateViewModel.validateFirstName(text.toString()) // Re-valider le champ
                }
            }
        }

        // Listener pour Surname
        binding.addCandidateSurname.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val uppercaseSurname = s.toString().uppercase() // Convertir en majuscule
                    if (s.toString() != uppercaseSurname) {
                        setText(uppercaseSurname) // Mettre à jour le texte avec la version en majuscule
                        setSelection(uppercaseSurname.length) // Remettre le curseur à la fin
                    }

                    // Valider le surname en majuscule
                    addCandidateViewModel.validateSurname(uppercaseSurname)
                    updateSaveButtonState() // Mettre à jour l'état du bouton de sauvegarde
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {  // Si le champ perd le focus
                    val uppercaseSurname = text.toString().uppercase() // Convertir en majuscule si nécessaire
                    setText(uppercaseSurname) // Mettre à jour le texte en majuscule
                    setSelection(uppercaseSurname.length) // Remettre le curseur à la fin
                    addCandidateViewModel.validateSurname(uppercaseSurname) // Re-valider le champ
                }
            }
        }

        // Listener pour Phone
        binding.addCandidatePhone.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    addCandidateViewModel.validatePhone(s.toString())
                    updateSaveButtonState()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {  // Si le champ perd le focus
                    addCandidateViewModel.validatePhone(text.toString()) // Re-valider le champ
                }
            }
        }

        // Listener pour Email
        binding.addCandidateMail.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    addCandidateViewModel.validateEmail(s.toString())
                    updateSaveButtonState()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {  // Si le champ perd le focus
                    addCandidateViewModel.validateEmail(text.toString()) // Re-valider le champ
                }
            }
        }

        // Listener pour Date of Birth
        binding.addCandidateBirth.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    // Valider la date de naissance dès que l'utilisateur tape
                    addCandidateViewModel.validateDateOfBirth(s.toString())
                    updateSaveButtonState() // Mettre à jour l'état du bouton "Save"
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Validation lorsque le champ perd le focus
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) { // Si le champ perd le focus
                    addCandidateViewModel.validateDateOfBirth(text.toString()) // Re-valider la date de naissance
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

        // Valider que tous les champs sont remplis
        if (firstName.isBlank() || surName.isBlank() || phone.isBlank() || email.isBlank() || selectedDate == null) {
            Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        // Formater la date de naissance en format "yyyy-MM-dd" via le ViewModel
        val formattedDate = addCandidateViewModel.formatDate(selectedDate)
        if (formattedDate == null) {
            Toast.makeText(requireContext(), "Invalid date", Toast.LENGTH_SHORT).show()
            return
        }

        // Parser la date formatée en LocalDateTime via le ViewModel
        val dateOfBirth = addCandidateViewModel.parseDateToLocalDateTime(formattedDate)
        if (dateOfBirth == null) {
            Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show()
            return
        }

        // Récupérer l'ID du candidat (ou 0 si c'est un ajout)
        val candidateId = arguments?.getLong("candidateId", -1L) ?: -1L

        // Si aucune nouvelle image n'est choisie, utiliser l'image actuelle (ancienne)
        val currentProfilePicture = addCandidateViewModel.imageCaptureState.value
            ?: getCurrentProfilePicture()


        // Préserver l'état "favori" du candidat actuel si nous modifions un candidat existant
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

        // Sauvegarder ou mettre à jour le candidat
        if (candidateId == -1L) {
            addCandidateViewModel.addCandidate(candidate)
            Toast.makeText(requireContext(), "Candidate added successfully!", Toast.LENGTH_SHORT)
                .show()
        } else {
            addCandidateViewModel.updateCandidate(candidate)
            Toast.makeText(requireContext(), "Candidate updated successfully!", Toast.LENGTH_SHORT)
                .show()
        }

        requireActivity().onBackPressedDispatcher.onBackPressed()
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


    private fun validateAllFields(): Boolean {
        val firstName = binding.addCandidateFirstname.text.toString().trim()
        val surName = binding.addCandidateSurname.text.toString().trim()
        val phone = binding.addCandidatePhone.text.toString().trim()
        val email = binding.addCandidateMail.text.toString().trim()
        val dateOfBirth = binding.addCandidateBirth.text.toString().trim()

        // Vérifier si tous les champs sont remplis et valides
        if (firstName.isBlank() || surName.isBlank() || phone.isBlank() || email.isBlank() || dateOfBirth.isBlank()) {
            return false
        }

        // Vérifier si l'email est valide
        if (!addCandidateViewModel.isEmailValid(email)) {
            return false
        }

        // Vérifier si la date de naissance est valide
        return addCandidateViewModel.isDateValid(dateOfBirth)
    }

    private fun updateSaveButtonState() {
        val isValid = validateAllFields()
        binding.addCandidateSaveButton.isEnabled = isValid // Ajuste le bouton correspondant
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