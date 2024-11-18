package com.example.vitesse.ui.addCandidate

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
     * Called to create the fragment's view. It initializes the binding and loads the candidate data
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

        setupToolbar()  // Initialize the toolbar
        observeUiState() // Observe UI state for error handling
        observeCandidateData() // Observe candidate data to pre-fill the form when editing

        // Set up actions for birth date picker, save button, and profile picture selection
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
                    // Pre-fill the form with the candidate's existing data (if available)
                    binding.addCandidateFirstname.setText(it.firstName)
                    binding.addCandidateSurname.setText(it.surName)
                    binding.addCandidatePhone.setText(it.phoneNumbers)
                    binding.addCandidateMail.setText(it.email)
                    binding.addCandidateSalary.setText(it.expectedSalaryEuros.toString())
                    binding.addCandidateNote.setText(it.note)
                    binding.addCandidateBirth.setText(it.dateOfBirth.toLocalDate().toString())

                    // Charger la date actuelle dans selectedDate
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
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Set the selected date when the user selects a date
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                binding.addCandidateBirth.setText(selectedDate.toString())
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    /**
     * Handles the selected image URI when the user selects a profile picture.
     * Decodes the image into a bitmap and passes it to the ViewModel.
     *
     * @param uri The URI of the selected image.
     */
    private fun handleImageUri(uri: Uri) {
        val imageStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(imageStream)
        binding.addCandidateProfilePicture.setImageBitmap(bitmap) // Affiche immédiatement l'image dans l'ImageView
        addCandidateViewModel.handleImageCapture(bitmap) // Stocke l'image dans le ViewModel pour la sauvegarde
    }

    /**
     * Handles saving or updating the candidate based on whether an ID was passed for editing.
     * Validates the input and creates a new candidate object, then calls the appropriate ViewModel method.
     */
    private fun handleSaveOrUpdateCandidate() {
        // Retrieve input values
        val firstName = binding.addCandidateFirstname.text.toString()
        val surName = binding.addCandidateSurname.text.toString()
        val phone = binding.addCandidatePhone.text.toString()
        val email = binding.addCandidateMail.text.toString()

        // Validate input
        if (firstName.isBlank() || surName.isBlank() || phone.isBlank() || email.isBlank()) {
            Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        // Retrieve the candidate ID (for editing) or set it to 0 for a new candidate
        val candidateId = arguments?.getLong("candidateId", -1L) ?: -1L
        val candidate = Candidate(
            firstName = firstName,
            surName = surName,
            phoneNumbers = phone,
            email = email,
            dateOfBirth = selectedDate?.atStartOfDay()
                ?: addCandidateViewModel.candidateFlow.value?.dateOfBirth  // Préserver la date actuelle
                ?: LocalDateTime.now(),
            expectedSalaryEuros = binding.addCandidateSalary.text.toString().toIntOrNull() ?: 0,
            note = binding.addCandidateNote.text.toString(),
            favorite = false,
            profilePicture = addCandidateViewModel.imageCaptureState.value
                ?: getCurrentProfilePicture(), // Use the captured image or the existing profile picture
            id = if (candidateId == -1L) 0 else candidateId
        )

        // If we're adding a new candidate, call the ViewModel to add it
        if (candidateId == -1L) {
            addCandidateViewModel.addCandidate(candidate)
            Toast.makeText(requireContext(), "Candidate added successfully!", Toast.LENGTH_SHORT)
                .show()
        } else {
            // If editing, update the existing candidate
            addCandidateViewModel.updateCandidate(candidate)
            Toast.makeText(requireContext(), "Candidate updated successfully!", Toast.LENGTH_SHORT)
                .show()
        }

        // Save the image if there was a change in the profile picture
        if (candidateId != -1L && addCandidateViewModel.imageCaptureState.value?.isNotEmpty() == true) {
            addCandidateViewModel.saveCandidateImage(candidateId)
        }

        // Navigate back and refresh the activity
        parentFragmentManager.popBackStack()
        requireActivity().recreate()
    }

    /**
     * Retrieves the current profile picture for the candidate, if available.
     * Returns an empty byte array if there is no profile picture.
     *
     * @return The current profile picture as a byte array.
     */
    private fun getCurrentProfilePicture(): ByteArray {
        return addCandidateViewModel.candidateFlow.value?.profilePicture ?: ByteArray(0)
    }

    /**
     * Factory method to create a new instance of the fragment with a candidate ID (for editing).
     *
     * @param candidateId The ID of the candidate to edit. Pass -1 for adding a new candidate.
     * @return A new instance of `AddCandidateFragment`.
     */
    companion object {
        fun newInstance(candidateId: Long): AddCandidateFragment {
            return AddCandidateFragment().apply {
                arguments = Bundle().apply {
                    putLong("candidateId", candidateId)
                }
            }
        }
    }
}
