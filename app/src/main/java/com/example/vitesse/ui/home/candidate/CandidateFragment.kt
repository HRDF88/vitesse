package com.example.vitesse.ui.home.candidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitesse.MainActivity
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.ui.detailsCandidate.DetailCandidateFragment
import com.example.vitesse.ui.interfaceUi.FilterableInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * A Fragment responsible for displaying a list of candidates and handling filtering,
 * updating candidate data, and navigation to a detailed view for each candidate.
 * This fragment observes the `CandidateViewModel` to retrieve and display a list of candidates,
 * manages user interactions, and handles the transition to a detail fragment.
 *
 * Implements [FilterableInterface] for filtering the candidates list based on a search query.
 */
@AndroidEntryPoint
class CandidateFragment : Fragment(), FilterableInterface {

    private lateinit var candidateAdapter: CandidateAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var candidateViewModel: CandidateViewModel

    private var filteredList: List<Candidate> = listOf()
    private var initialLayoutParams: ConstraintLayout.LayoutParams? = null

    private var isInDetailFragment = false

    /**
     * Called to create and inflate the view for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_candidate, container, false)
    }

    /**
     * Called when the fragment's view has been created. Sets up the necessary UI components,
     * starts observing the view model for updates, and initializes the RecyclerView and Adapter.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Listen for DetailCandidateFragment updates
        parentFragmentManager.setFragmentResultListener("candidate_updated", this) { _, bundle ->
            val isUpdated = bundle.getBoolean("updated", false)
            if (isUpdated) {
                candidateViewModel.getAllCandidates() // Reload the list of candidates
            }
        }

        val fragmentContainerView =
            requireActivity().findViewById<FragmentContainerView>(R.id.main_view)
        initialLayoutParams = fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams

        recyclerView = view.findViewById(R.id.candidate_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        candidateAdapter = CandidateAdapter(emptyList()) { candidate ->
            openDetailFragment(candidate)
        }
        recyclerView.adapter = candidateAdapter

        candidateViewModel = ViewModelProvider(this).get(CandidateViewModel::class.java)


        // Observe candidates
        viewLifecycleOwner.lifecycleScope.launch {
            candidateViewModel.candidateFlow.collect { candidates ->
                filteredList = candidates.sortedBy { it.surName }
                candidateAdapter.updateData(filteredList)
                updateUI(candidates)
            }
        }

        // Observe UI state to handle loading or errors
        viewLifecycleOwner.lifecycleScope.launch {
            candidateViewModel.uiState.collect { uiState ->

                if (uiState.isLoading) {
                    showLoading(true)
                } else {
                    showLoading(false)

                    if (uiState.error.isNotEmpty()) {
                        Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                        candidateViewModel.updateErrorState()
                    }
                }
            }
        }

        // Initial call to load candidates
        candidateViewModel.getAllCandidates()
    }

    /**
     * Called when the fragment is resumed. It triggers the reloading of candidate data.
     * Adjusts the layout of the container view when returning from the detail fragment.
     */
    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            candidateViewModel.getAllCandidates()
        }

        if (activity is MainActivity) {
            val mainActivity = activity as MainActivity
            if (!isInDetailFragment) {
                mainActivity.adjustFragmentContainerViewLayout(false)
            }
        }
    }

    /**
     * Called when the fragment is paused. Updates the flag indicating if the fragment
     * is in the detail view.
     */
    override fun onPause() {
        super.onPause()
        isInDetailFragment = false
    }

    /**
     * Filters the list of candidates based on the provided query.
     *
     * @param query The search query to filter the candidate list.
     */
    override fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            candidateViewModel.candidateFlow.value
        } else {
            candidateViewModel.candidateFlow.value.filter {
                it.firstName.contains(query, ignoreCase = true) || it.surName.contains(
                    query,
                    ignoreCase = true
                )
            }
        }

        candidateAdapter.updateData(filteredList)
    }

    /**
     * Opens the detailed view of a candidate by navigating to the [DetailCandidateFragment].
     *
     * @param candidate The candidate whose details will be displayed.
     */
    private fun openDetailFragment(candidate: Candidate) {
        val detailFragment = DetailCandidateFragment.newInstance(candidate.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_view, detailFragment)
            .addToBackStack(null)
            .commit()
        (activity as MainActivity).adjustFragmentContainerViewLayout(true)
        isInDetailFragment = true
    }

    /**
     * Toggles the visibility of the loading indicator (ProgressBar).
     *
     * @param isLoading True if the loading indicator should be visible, false otherwise.
     */
    private fun showLoading(isLoading: Boolean) {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun updateUI(candidates: List<Candidate>) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.candidate_recyclerView)
        val emptyMessage = view?.findViewById<TextView>(R.id.empty_message)

        if (candidates.isEmpty()) {
            recyclerView?.visibility = View.GONE
            emptyMessage?.visibility = View.VISIBLE
        } else {
            recyclerView?.visibility = View.VISIBLE
            emptyMessage?.visibility = View.GONE
        }
    }
}