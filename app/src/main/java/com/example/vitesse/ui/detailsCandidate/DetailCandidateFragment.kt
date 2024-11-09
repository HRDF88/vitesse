package com.example.vitesse.ui.detailsCandidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.vitesse.databinding.FragmentDetailCandidateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DetailCandidateFragment : Fragment() {

    private var candidateId: Long? = null
    private lateinit var binding: FragmentDetailCandidateBinding
    private val detailCandidateViewModel: DetailCandidateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        candidateId = arguments?.getLong(ARG_CANDIDATE_ID)

        // Charger les détails du candidat lorsque l'ID est disponible
        candidateId?.let {
            detailCandidateViewModel.loadCandidateDetails(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Utiliser le ViewBinding pour lier le layout
        binding = FragmentDetailCandidateBinding.inflate(inflater, container, false)
        viewLifecycleOwner.lifecycleScope.launch {
            // Observer l'état du ViewModel pour les mises à jour de l'UI
            detailCandidateViewModel.uiState.collect() { uiState ->
                // Gérer l'état de chargement
                binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE

                // Mettre à jour les vues si le candidat est disponible
                uiState.candidate?.let { candidate ->
                    binding.detailCandidateName.text = "${candidate.firstName} ${candidate.surName}"
                    binding.detailCandidateNote.text = candidate.note
                    binding.detailCandidateDateOfBirth.text = candidate.dateOfBirth.toString()
                    binding.detailCandidateExpectedSalaryEuros.text = candidate.expectedSalaryEuros.toString()
                    /*
                    binding.detailCandidateExpectedSalaryPounds.text =
                        candidate.expectedSalaryPounds*/

                    // Afficher l'âge calculé pour ce candidat
                    val candidateAge = detailCandidateViewModel.candidatesWithAge.value
                        .find { it.candidate.id == candidate.id }?.age ?: "Âge inconnu"
                    binding.detailCandidateAge.text = "($candidateAge ans)"
                }

                // Afficher les erreurs s'il y en a
                if (uiState.error.isNotBlank()) {
                    Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                    // Reset the error state
                    detailCandidateViewModel.updateErrorState("")
                }
            }


        }
        return binding.root
    }


    companion object {
        private const val ARG_CANDIDATE_ID = "candidate_id"

        // Crée une instance du fragment en passant l'ID du candidat en argument
        fun newInstance(candidateId: Long): DetailCandidateFragment {
            val fragment = DetailCandidateFragment()
            val args = Bundle()
            args.putLong(ARG_CANDIDATE_ID, candidateId)
            fragment.arguments = args
            return fragment
        }
    }
}