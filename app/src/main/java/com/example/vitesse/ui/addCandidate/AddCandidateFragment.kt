package com.example.vitesse.ui.addCandidate

import android.app.DatePickerDialog
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * A fragment that allows users to add or edit a candidate's information.
 * If a valid candidate ID is provided in the arguments, the fragment will load the existing
 * candidate data for editing. Otherwise, it allows adding a new candidate.
 */
@AndroidEntryPoint
class AddCandidateFragment : Fragment() {

    private lateinit var binding: FragmentAddCandidateBinding
    private val addCandidateViewModel: AddCandidateViewModel by viewModels()

    private var selectedDate: LocalDate? = null  // Pour stocker la date sélectionnée

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
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        // Observe the UI state for error handling and updating the UI
        viewLifecycleOwner.lifecycleScope.launch {
            addCandidateViewModel.uiState.collect { uiState ->
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
                    binding.addCandidateFirstname.setText(it.firstName)
                    binding.addCandidateSurname.setText(it.surName)
                    binding.addCandidatePhone.setText(it.phoneNumbers)
                    binding.addCandidateMail.setText(it.email)
                    binding.addCandidateSalary.setText(it.expectedSalaryEuros.toString())
                    binding.addCandidateNote.setText(it.note)

                    // Set the birth date (showing only the date part)
                    binding.addCandidateBirth.setText(it.dateOfBirth.toLocalDate().toString())
                }
            }
        }

        // Handle the DatePickerDialog for birth date selection
        binding.addCandidateBirth.setOnClickListener {
            showDatePickerDialog()
        }

        // Configure the save button
        binding.addCandidateSaveButton.setOnClickListener {
            val firstName = binding.addCandidateFirstname.text.toString()
            val surName = binding.addCandidateSurname.text.toString()
            val phone = binding.addCandidatePhone.text.toString()
            val email = binding.addCandidateMail.text.toString()

            if (firstName.isBlank() || surName.isBlank() || phone.isBlank() || email.isBlank()) {
                Toast.makeText(requireContext(), "Tous les champs doivent être remplis", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val candidateId = arguments?.getLong("candidateId", -1L) ?: -1L

            // Create the Candidate object
            val candidate = Candidate(
                firstName = firstName,
                surName = surName,
                phoneNumbers = phone,
                email = email,
                dateOfBirth = selectedDate?.atStartOfDay() ?: LocalDateTime.now().withHour(0).withMinute(0).withSecond(0),  // Si aucune date n'est sélectionnée, on met la date actuelle à minuit
                expectedSalaryEuros = try {
                    binding.addCandidateSalary.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    0  // Default salary value
                },
                note = binding.addCandidateNote.text.toString(),
                favorite = false,
                profilePicture = "test",  // Profile picture is not handled in this example
                id = if (candidateId == -1L) 0 else candidateId  // If ID is -1 (add), set to 0, else use existing ID
            )

            if (candidateId == -1L) {
                addCandidateViewModel.addCandidate(candidate)
                Toast.makeText(requireContext(), "Candidat ajouté avec succès!", Toast.LENGTH_SHORT).show()
            } else {
                addCandidateViewModel.updateCandidate(candidate)
                Toast.makeText(requireContext(), "Candidat modifié avec succès!", Toast.LENGTH_SHORT).show()
            }

            parentFragmentManager.popBackStack()
            requireActivity().recreate()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Create a LocalDate with the selected date
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth) // month is 0-based
                binding.addCandidateBirth.setText(selectedDate.toString())  // Show the date in the format yyyy-MM-dd
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    companion object {
        fun newInstance(candidateId: Long): AddCandidateFragment {
            val fragment = AddCandidateFragment()
            val args = Bundle().apply {
                putLong("candidateId", candidateId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
