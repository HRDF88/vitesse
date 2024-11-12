package com.example.vitesse.ui.home.candidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.ui.detailsCandidate.DetailCandidateFragment
import com.example.vitesse.ui.interfaceUi.FilterableInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CandidateFragment : Fragment(), FilterableInterface {

    private lateinit var candidateAdapter: CandidateAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var candidateViewModel: CandidateViewModel

    private var filteredList: List<Candidate> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_candidate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser RecyclerView et l'adaptateur
        recyclerView = view.findViewById(R.id.candidate_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        candidateAdapter = CandidateAdapter(emptyList()) { candidate ->
            openDetailFragment(candidate)
        }
        recyclerView.adapter = candidateAdapter

        // Initialiser le ViewModel avec Hilt
        candidateViewModel = ViewModelProvider(this).get(CandidateViewModel::class.java)

        // Observer des candidats avec collect dans une coroutine
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            candidateViewModel.candidateFlow.collect { candidates ->
                // Mettre à jour la liste des candidats
                filteredList = candidates
                candidateAdapter.updateData(filteredList)
            }
        }

        // Observer de l'état de l'UI pour gérer le chargement ou les erreurs
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            candidateViewModel.uiState.collect { uiState ->
                // Afficher une animation de chargement ou une erreur si nécessaire
                if (uiState.isLoading) {
                    // Afficher un ProgressBar ou autre indication de chargement
                    showLoading(true)
                } else {
                    // Cacher le ProgressBar
                    showLoading(false)

                    // Si une erreur est présente, afficher un message
                    if (uiState.error.isNotEmpty()) {
                        Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Récupérer les candidats
        candidateViewModel.getAllCandidates()
    }

    override fun onResume() {
        super.onResume()
        // Call getAllCandidate to refresh the list when the fragment is resumed
        viewLifecycleOwner.lifecycleScope.launch {
        candidateViewModel.getAllCandidates()
        }
    }
    /**
     * Filtre la liste des candidats en fonction de la requête de recherche.
     */
    override fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            candidateViewModel.candidateFlow.value // Liste complète si le filtre est vide
        } else {
            candidateViewModel.candidateFlow.value.filter {
                it.firstName.contains(query, ignoreCase = true) || it.surName.contains(query, ignoreCase = true)
            }
        }

        candidateAdapter.updateData(filteredList) // Mettre à jour les données filtrées
    }

    /**
     * Ouvre un fragment pour afficher les détails d'un candidat.
     */
    private fun openDetailFragment(candidate: Candidate) {
        val detailFragment = DetailCandidateFragment.newInstance(candidate.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_view, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Affiche ou cache un indicateur de chargement (ProgressBar).
     */
    private fun showLoading(isLoading: Boolean) {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar) // Assurez-vous que vous avez un ProgressBar dans votre layout
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
