package com.example.vitesse.ui.home.favoriteCandidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitesse.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteCandidateFragment : Fragment() {

    private lateinit var favoriteCandidateAdapter: FavoriteCandidateAdapter
    private lateinit var recyclerView: RecyclerView
    private val favoriteCandidateViewModel: FavoriteCandidateViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_candidate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.favorite_candidate_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialiser l'adaptateur avec une liste vide pour commencer
        favoriteCandidateAdapter = FavoriteCandidateAdapter()
        recyclerView.adapter = favoriteCandidateAdapter

        // Charger les candidats favoris dès que le fragment est prêt (fonction suspendue)
        viewLifecycleOwner.lifecycleScope.launch {
            // Charger les candidats favoris
            favoriteCandidateViewModel.loadFavoriteCandidate()

            // Collecte de favoriteCandidateFlow et uiState dans une seule coroutine
            launch {
                // Collecte des candidats
                favoriteCandidateViewModel.favoriteCandidateFlow.collect { candidates ->
                    // Mettre à jour l'adaptateur avec les nouveaux candidats
                    favoriteCandidateAdapter.uptdateData(candidates)
                }
            }

            launch {
                // Collecte de l'état UI (pour gérer les erreurs)
                favoriteCandidateViewModel.uiState.collect { uiState ->
                    if (uiState.error.isNotBlank()) {
                        Toast.makeText(
                            requireContext(),
                            uiState.error,
                            Toast.LENGTH_LONG
                        ).show()
                        favoriteCandidateViewModel.updateErrorState("")  // Réinitialise l'état d'erreur
                    }
                }
            }
        }
    }
}

