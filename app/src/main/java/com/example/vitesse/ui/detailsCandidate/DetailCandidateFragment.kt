package com.example.vitesse.ui.detailsCandidate

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.vitesse.R
import com.example.vitesse.databinding.FragmentDetailCandidateBinding
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.ui.addCandidate.AddCandidateFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment that displays the details of a specific candidate.
 * Uses Hilt for dependency injection.
 * Loads and observes candidate data from a ViewModel and updates the UI accordingly.
 *
 * @constructor Creates a new instance of `DetailCandidateFragment`.
 */
@AndroidEntryPoint
class DetailCandidateFragment : Fragment() {

    private var candidateId: Long? = null
    private lateinit var binding: FragmentDetailCandidateBinding
    private val detailCandidateViewModel: DetailCandidateViewModel by viewModels()
    private var isFavorite: Boolean = false
    private lateinit var favoriteMenuItem: MenuItem // Reference for the favorite menu item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        candidateId = arguments?.getLong(ARG_CANDIDATE_ID)
        candidateId?.let {
            detailCandidateViewModel.loadCandidateDetails(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailCandidateBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true) // Enable options menu
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar() // Set up the toolbar
        setupMenuProvider() // Set up the menu for the fragment
        observeCandidateDetails() // Observe candidate details from the ViewModel

        // Associez les boutons aux actions
        binding.detailCandidateCall.setOnClickListener {
            val candidate = detailCandidateViewModel.uiState.value.candidate
            val phoneNumber = candidate?.phoneNumbers
            phoneNumber?.let { phone ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                startActivity(intent) // Ouvre l'application téléphone
            }
        }

        binding.detailCandidateSms.setOnClickListener {
            val candidate = detailCandidateViewModel.uiState.value.candidate
            val phoneNumber = candidate?.phoneNumbers
            phoneNumber?.let { phone ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone"))
                startActivity(intent) // Ouvre l'application SMS
            }
        }

        binding.detailCandidateEmail.setOnClickListener {
            val candidate = detailCandidateViewModel.uiState.value.candidate
            val email = candidate?.email
            email?.let { mail ->
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$mail"))
                intent.putExtra(Intent.EXTRA_SUBJECT, "Subject") // Sujet de l'email
                intent.putExtra(Intent.EXTRA_TEXT, "Body of the email") // Corps de l'email
                startActivity(intent) // Ouvre l'application de mail
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // Recharger les informations du candidat après une modification
        candidateId?.let {
            detailCandidateViewModel.loadCandidateDetails(it) // Recharger les données dans onResume
        }
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.root.findViewById(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar) // Set the toolbar as the ActionBar

        // Set the back icon and its click listener
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            // Avant de revenir, ajuster la taille du FragmentContainerView
            adjustFragmentContainerViewLayout(false)
            parentFragmentManager.popBackStack()
        }
    }

    /**
     * Ajuste les dimensions du FragmentContainerView avant de revenir au fragment précédent.
     */
    private fun adjustFragmentContainerViewLayout(isDetailFragment: Boolean) {
        val fragmentContainerView =
            requireActivity().findViewById<FragmentContainerView>(R.id.main_view)
        val layoutParams = fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
        if (isDetailFragment) {
            layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
        } else {
            layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        }
        fragmentContainerView.layoutParams = layoutParams
    }

    private fun setupMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_detail_candidate, menu) // Inflate the menu
                // Initialiser le favoriteMenuItem
                favoriteMenuItem = menu.findItem(R.id.menu_favorite)
                updateFavoriteMenuIcon() // Appeler après l'initialisation
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_favorite -> {
                        toggleFavorite() // Toggle favorite status
                        true
                    }

                    R.id.menu_edit -> {
                        navigateToAddCandidateFragment() // Navigate to add/edit candidate fragment
                        true
                    }

                    R.id.menu_delete -> {
                        showDeleteConfirmationDialog() // Show delete confirmation dialog
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner) // Use the view lifecycle to manage the menu
    }


    private fun observeCandidateDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            detailCandidateViewModel.uiState.collect { uiState ->
                binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                uiState.candidate?.let { candidate ->
                    (activity as? AppCompatActivity)?.supportActionBar?.title =
                        "${candidate.firstName} ${candidate.surName}" // Set the title in the ActionBar
                    updateCandidateDetails(candidate)
                }

                if (uiState.error.isNotBlank()) {
                    showError(uiState.error) // Show error message
                    detailCandidateViewModel.updateErrorState("") // Reset the error state
                }

                if (uiState.isDeleted) {
                    showDeletionSuccess() // Show success message after deletion
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateCandidateDetails(candidate: Candidate) {
        binding.detailCandidateNote.text = candidate.note
        binding.detailCandidateDateOfBirth.text = candidate.dateOfBirth.toString()
        binding.detailCandidateExpectedSalaryEuros.text = candidate.expectedSalaryEuros.toString()
        isFavorite = candidate.favorite
        updateFavoriteMenuIcon()

        val candidateAge = detailCandidateViewModel.candidatesWithAge.value
            .find { it.candidate.id == candidate.id }?.age ?: getString(R.string.age_unknown)
        binding.detailCandidateAge.text = getString(R.string.year, candidateAge)
    }

    private fun toggleFavorite() {
        val candidate = detailCandidateViewModel.uiState.value.candidate
        candidate?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                if (isFavorite) {
                    detailCandidateViewModel.deleteFavoriteCandidate(it)
                } else {
                    detailCandidateViewModel.addNewFavoriteCandidate(it)
                }
            }
            isFavorite = !isFavorite
            updateFavoriteMenuIcon() // Update the menu's favorite icon
        }
    }


    private fun updateFavoriteMenuIcon() {
        // Vérifier que favoriteMenuItem est bien initialisé avant de l'utiliser
        if (::favoriteMenuItem.isInitialized) {
            favoriteMenuItem.icon = ContextCompat.getDrawable(
                requireContext(),
                if (isFavorite) R.drawable.star_full else R.drawable.star
            )
        } else {
            Log.e(TAG, "favoriteMenuItem n'est pas initialisé")
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_deletion))
            .setMessage(getString(R.string.question_delete_candidate))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                adjustFragmentContainerViewLayout(false)
                detailCandidateViewModel.uiState.value.candidate?.let { candidate ->
                    detailCandidateViewModel.deleteCandidate(candidate)
                }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showDeletionSuccess() {
        Toast.makeText(requireContext(), getString(R.string.delete_candidate), Toast.LENGTH_SHORT)
            .show()
    }

    private fun navigateToAddCandidateFragment() {
        val candidate = detailCandidateViewModel.uiState.value.candidate
        candidate?.let {
            // Créer une instance du fragment à ouvrir
            val addCandidateFragment =
                AddCandidateFragment.newInstance(it.id) // Passez l'ID du candidat ou d'autres données nécessaires

            // Remplacer le fragment actuel par le nouveau fragment
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.main_view,
                    addCandidateFragment
                ) // Remplacer le fragment actuel par AddCandidateFragment
                .addToBackStack(null) // Ajoute à la pile arrière pour que l'utilisateur puisse revenir en arrière
                .commit()
        }
    }

    companion object {
        private const val ARG_CANDIDATE_ID = "candidate_id"

        fun newInstance(candidateId: Long): DetailCandidateFragment {
            return DetailCandidateFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CANDIDATE_ID, candidateId)
                }
            }
        }
    }
}
