package com.example.vitesse.ui.home.candidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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

@AndroidEntryPoint
class CandidateFragment : Fragment(), FilterableInterface {

    private lateinit var candidateAdapter: CandidateAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var candidateViewModel: CandidateViewModel

    private var filteredList: List<Candidate> = listOf()
    private var initialLayoutParams: ConstraintLayout.LayoutParams? = null

    private var isInDetailFragment = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_candidate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // Observer des candidats
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            candidateViewModel.candidateFlow.collect { candidates ->
                filteredList = candidates
                candidateAdapter.updateData(filteredList)
            }
        }

        // Observer de l'état de l'UI pour gérer le chargement ou les erreurs
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            candidateViewModel.uiState.collect { uiState ->
                if (uiState.isLoading) {
                    showLoading(true)
                } else {
                    showLoading(false)

                    if (uiState.error.isNotEmpty()) {
                        Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Initial call to load candidates
        candidateViewModel.getAllCandidates()
    }


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

    override fun onPause() {
        super.onPause()
        isInDetailFragment = false
    }

    override fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            candidateViewModel.candidateFlow.value // Liste complète si le filtre est vide
        } else {
            candidateViewModel.candidateFlow.value.filter {
                it.firstName.contains(query, ignoreCase = true) || it.surName.contains(query, ignoreCase = true)
            }
        }

        candidateAdapter.updateData(filteredList)
    }

    private fun openDetailFragment(candidate: Candidate) {
        val detailFragment = DetailCandidateFragment.newInstance(candidate.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_view, detailFragment)
            .addToBackStack(null)
            .commit()
        (activity as MainActivity).adjustFragmentContainerViewLayout(true)
        isInDetailFragment = true
    }

    private fun showLoading(isLoading: Boolean) {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}