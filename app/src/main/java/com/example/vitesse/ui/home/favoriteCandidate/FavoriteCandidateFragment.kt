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
import com.example.vitesse.ui.interfaceUi.FilterableInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
/**
 * Fragment that displays a list of favorite candidates in a RecyclerView.
 * Implements the FilterableInterface to allow filtering of the favorite candidates based on search query.
 */
class FavoriteCandidateFragment : Fragment(), FilterableInterface {

    private lateinit var favoriteCandidateAdapter: FavoriteCandidateAdapter
    private lateinit var recyclerView: RecyclerView
    private val favoriteCandidateViewModel: FavoriteCandidateViewModel by viewModels()

    /**
     * Called to inflate the fragment's layout.
     *
     * @param inflater The LayoutInflater object to inflate views.
     * @param container The parent ViewGroup that the fragment's UI will be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_candidate, container, false)
    }

    /**
     * Called after the fragment's view has been created.
     * Initializes the RecyclerView, sets up the adapter, and binds it to the UI.
     * Also fetches and observes the favorite candidates from the ViewModel.
     *
     * @param view The view returned by onCreateView.
     * @param savedInstanceState The state saved during the fragment's previous lifecycle.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and its adapter
        recyclerView = view.findViewById(R.id.favorite_candidate_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter with an empty list to start
        favoriteCandidateAdapter = FavoriteCandidateAdapter()
        recyclerView.adapter = favoriteCandidateAdapter

        // Load favorite candidates as soon as the fragment is ready (suspended function)
        viewLifecycleOwner.lifecycleScope.launch {
            // Load favorite candidates from the ViewModel
            favoriteCandidateViewModel.loadFavoriteCandidate()

            // Collect favoriteCandidateFlow and uiState in one coroutine
            launch {
                // Collect the list of favorite candidates
                favoriteCandidateViewModel.favoriteCandidateFlow.collect { candidates ->
                    // Update the adapter with the new list of candidates
                    favoriteCandidateAdapter.updateData(candidates)
                }
            }

            launch {
                // Collect UI state (for error handling)
                favoriteCandidateViewModel.uiState.collect { uiState ->
                    if (uiState.error.isNotBlank()) {
                        // Show a toast if there is an error
                        Toast.makeText(
                            requireContext(),
                            uiState.error,
                            Toast.LENGTH_LONG
                        ).show()
                        // Reset the error state
                        favoriteCandidateViewModel.updateErrorState("")
                    }
                }
            }
        }
    }

    /**
     * Filters the list of favorite candidates based on the search query.
     * This method is part of the FilterableInterface, which allows it to be called from other parts of the app.
     *
     * @param query The search query to filter the candidates.
     * The filter method is called on the adapter to apply the query on the favorite candidates list.
     */
    override fun filter(query: String) {
        favoriteCandidateAdapter.filter(query) // Call the filter method of the adapter to apply the filter
    }
}

