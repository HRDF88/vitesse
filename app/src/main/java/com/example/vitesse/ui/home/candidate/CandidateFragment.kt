package com.example.vitesse.ui.home.candidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vitesse.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CandidateFragment : Fragment() {
    private lateinit var candidateAdapter: CandidateAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_candidate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.candidate_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        candidateAdapter=CandidateAdapter(emptyList())
        recyclerView.adapter = candidateAdapter
    }

}
