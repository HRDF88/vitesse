package com.example.vitesse.ui.addCandidate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.vitesse.MainActivity
import com.example.vitesse.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddCandidateFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_candidate, container, false)
        Log.d("AddCandidateFragment", "Fragment view created")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AddCandidateFragment", "Fragment view created")
        // Vérifiez si la vue est correctement affichée
        if (view.visibility == View.VISIBLE) {
            Log.d("AddCandidateFragment", "Fragment's root view is visible")
        } else {
            Log.d("AddCandidateFragment", "Fragment's root view is not visible")
        }


        // Activer la flèche de retour dans la Toolbar
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.apply {
            setNavigationIcon(R.drawable.arrow_back)
            setNavigationOnClickListener {
                // Retour en arrière lorsque l'utilisateur appuie sur la flèche de retour
                requireActivity().onBackPressedDispatcher.onBackPressed()

            }
        }
    }
}
