package com.example.vitesse.ui.addCandidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.vitesse.R
import com.example.vitesse.databinding.FragmentAddCandidateBinding
import com.example.vitesse.domain.model.Candidate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A fragment that allows users to add or edit a candidate's information.
 * If a valid candidate ID is provided in the arguments, the fragment will load the existing
 * candidate data for editing. Otherwise, it allows adding a new candidate.
 */
@AndroidEntryPoint
class AddCandidateFragment : Fragment() {

    private lateinit var binding: FragmentAddCandidateBinding
    private val addCandidateViewModel: AddCandidateViewModel by viewModels()

    /**
     * Inflates the layout for the fragment and retrieves the candidate ID from arguments.
     * If the ID is valid, it loads the candidate data for editing.
     *
     * @param inflater The LayoutInflater object that can be used to inflate the layout.
     * @param container The parent container in which the fragment's UI should be placed.
     * @param savedInstanceState A bundle containing the saved state, if any.
     * @return The root view of the fragment's layout.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for the fragment
        binding = FragmentAddCandidateBinding.inflate(inflater, container, false)

        // Retrieve the candidate ID from arguments, defaulting to -1L if not found
        val candidateId = arguments?.getLong("candidateId", -1L) ?: -1L

        // Load the candidate data if the ID is valid (for edit mode)
        if (candidateId != -1L) {
            addCandidateViewModel.loadCandidateById(candidateId)
        }

        return binding.root
    }

    /**
     * Sets up the necessary UI components and observers for handling user input and displaying candidate data.
     * Observes candidate data and the UI state for error handling and updates.
     *
     * @param view The root view of the fragment.
     * @param savedInstanceState A bundle containing the saved state, if any.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the toolbar with back navigation
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.apply {
            val candidateId = arguments?.getLong("candidateId", -1L) ?: -1L
            title = if (candidateId == -1L) {
                getString(R.string.add_candidate)
            } else {
                getString(R.string.edit_candidate)
            }
            setNavigationIcon(R.drawable.arrow_back)
            setNavigationOnClickListener {
                // Handle back navigation when the user presses the back arrow
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        // Observe the UI state for error handling and updating the UI
        viewLifecycleOwner.lifecycleScope.launch {
            addCandidateViewModel.uiState.collect { uiState ->
                // If there's an error, show a toast message
                if (uiState.error.isNotBlank()) {
                    Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                    addCandidateViewModel.updateErrorState("")  // Reset the error state
                }
            }
        }

        // Observe the candidate data for editing (if in edit mode)
        viewLifecycleOwner.lifecycleScope.launch {
            addCandidateViewModel.candidateFlow.collect { candidate ->
                candidate?.let {
                    // Set the values in the form if editing an existing candidate
                    binding.addCandidateFirstname.setText(it.firstName)
                    binding.addCandidateSurname.setText(it.surName)
                    binding.addCandidatePhone.setText(it.phoneNumbers)
                    binding.addCandidateMail.setText(it.email)
                    binding.addCandidateBirth.setText(
                        it.dateOfBirth.toLocalDate().toString()
                    )  // Display only the date (no time)
                    binding.addCandidateSalary.setText(it.expectedSalaryEuros.toString())
                    binding.addCandidateNote.setText(it.note)
                }
            }
        }

        /**
         * Configures the save button click listener to either add or update the candidate.
         * If a valid candidate ID is provided, the existing candidate will be updated.
         * Otherwise, a new candidate will be added.
         */
        binding.addCandidateSaveButton.setOnClickListener {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val candidate = Candidate(
                firstName = binding.addCandidateFirstname.text.toString(),
                surName = binding.addCandidateSurname.text.toString(),
                phoneNumbers = binding.addCandidatePhone.text.toString(),
                email = binding.addCandidateMail.text.toString(),
                dateOfBirth = try {
                    // Parse the birth date and append the time to set it to 00:00:00
                    LocalDateTime.parse(
                        binding.addCandidateBirth.text.toString() + "T00:00:00",
                        formatter
                    )
                } catch (e: Exception) {
                    // Default to current date with time set to 00:00 if parsing fails
                    LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
                },
                expectedSalaryEuros = try {
                    // Convert salary to integer or default to 0 if parsing fails
                    binding.addCandidateSalary.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    0  // Default salary value
                },
                note = binding.addCandidateNote.text.toString(),
                favorite = false,
                profilePicture = "test",  // Profile picture is not handled in this example
                id = 0  // ID will be handled by the database (auto-generated)
            )

            // Check if we are editing or adding a new candidate
            if (arguments?.getLong("candidateId", -1L) == -1L) {
                // Add new candidate if ID is invalid (-1L)
                addCandidateViewModel.addCandidate(candidate)
                Toast.makeText(requireContext(), "Candidat ajouté avec succès!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Update existing candidate if ID is valid
                addCandidateViewModel.updateCandidate(candidate)
                Toast.makeText(requireContext(), "Candidat modifié avec succès!", Toast.LENGTH_SHORT)
                    .show()
            }
            parentFragmentManager.popBackStack()

        }
    }

    companion object {
        // Cette méthode permet de créer une nouvelle instance du fragment en passant des arguments (comme l'ID du candidat)
        fun newInstance(candidateId: Long): AddCandidateFragment {
            val fragment = AddCandidateFragment()
            val args = Bundle().apply {
                putLong("candidateId", candidateId) // Passez l'ID du candidat en argument
            }
            fragment.arguments = args // Attache les arguments au fragment
            return fragment
        }
    }


}