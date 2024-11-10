package com.example.vitesse.ui.home.candidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.ui.detailsCandidate.DetailCandidateFragment
import com.example.vitesse.ui.interfaceUi.FilterableInterface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
/**
 * Fragment that displays a list of candidates in a RecyclerView.
 * Implements the FilterableInterface to allow filtering of candidates based on search query.
 */
class CandidateFragment : Fragment(), FilterableInterface {

    private lateinit var candidateAdapter: CandidateAdapter
    private lateinit var recyclerView: RecyclerView
    private var candidateList: List<Candidate> = listOf() // List of all candidates
    private var filteredList = candidateList // List of candidates after applying the filter

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
        return inflater.inflate(R.layout.fragment_candidate, container, false)
    }

    /**
     * Called after the fragment's view has been created.
     * Initializes the RecyclerView, sets up the adapter, and binds it to the UI.
     *
     * @param view The view returned by onCreateView.
     * @param savedInstanceState The state saved during the fragment's previous lifecycle.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and its adapter
        recyclerView = view.findViewById(R.id.candidate_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter with an empty list to start
        candidateAdapter = CandidateAdapter(emptyList()) { candidate ->
            openDetailFragment(candidate)
        }
        recyclerView.adapter = candidateAdapter
    }

    /**
     * Filters the candidate list based on the search query.
     * This method is part of the FilterableInterface, which allows it to be called from other parts of the app.
     *
     * @param query The search query to filter the candidates.
     * If the query is empty, the full list is returned. Otherwise, candidates whose first or last names
     * contain the query (case-insensitive) are included in the filtered list.
     */
    override fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            candidateList // If the query is empty, return the whole list
        } else {
            // Filter candidates whose first name or last name contains the query (case-insensitive)
            candidateList.filter {
                it.firstName.contains(query, ignoreCase = true) || it.surName.contains(
                    query,
                    ignoreCase = true
                )
            }
        }

        // Update the adapter with the filtered list of candidates
        candidateAdapter.updateData(filteredList)
    }

    /**
     * Opens a detail fragment to display information about a given candidate.
     *
     * This function creates an instance of `DetailCandidateFragment` using the ID
     * of the provided candidate, then replaces the current fragment in the container
     * specified by `R.id.main_view` with the detail fragment. The fragment transaction
     * is added to the back stack to enable backward navigation.
     *
     * @param candidate The candidate whose detail fragment will be displayed.
     *                  The `Candidate` object must contain a unique ID to identify the candidate.
     */
    private fun openDetailFragment(candidate: Candidate) {
        val detailFragment = DetailCandidateFragment.newInstance(candidate.id)
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.main_view,
                detailFragment
            ) // Remplacez `R.id.main_view` par l'ID de votre conteneur
            .addToBackStack(null) // Ajoute à la pile arrière pour permettre le retour en arrière
            .commit()
    }
}
