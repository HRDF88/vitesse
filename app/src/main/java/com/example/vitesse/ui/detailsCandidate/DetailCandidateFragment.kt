package com.example.vitesse.ui.detailsCandidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.vitesse.R
import com.example.vitesse.databinding.FragmentDetailCandidateBinding
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

    // The ID of the candidate whose details are displayed.
    private var candidateId: Long? = null

    // Binding for the XML layout associated with this fragment.
    private lateinit var binding: FragmentDetailCandidateBinding

    // ViewModel to manage candidate data Logic.
    private val detailCandidateViewModel: DetailCandidateViewModel by viewModels()

    // Track if the candidate is in favorites or not.
    private var isFavorite: Boolean = false

    /**
     * Initializes the fragment and loads candidate details if an ID is available.
     *
     * @param savedInstanceState The previous saved state of the fragment, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        candidateId = arguments?.getLong(ARG_CANDIDATE_ID)

        // Load candidate details if the ID is available.
        candidateId?.let {
            detailCandidateViewModel.loadCandidateDetails(it)
        }
    }

    /**
     * Creates and initializes the fragment's view. Uses ViewBinding to bind UI elements.
     * Observes the `uiState` from the ViewModel and updates the UI based on state changes.
     *
     * @param inflater LayoutInflater object to inflate views in the fragment.
     * @param container The parent view that will contain this fragment's view.
     * @param savedInstanceState The previous saved state of the fragment, if any.
     * @return The root view of the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Use ViewBinding to bind the layout.
        binding = FragmentDetailCandidateBinding.inflate(inflater, container, false)
        viewLifecycleOwner.lifecycleScope.launch {

            // Observe ViewModel state for UI updates.
            detailCandidateViewModel.uiState.collect() { uiState ->

                // Handle loading state.
                binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE

                // Update views if candidate details are available.
                uiState.candidate?.let { candidate ->
                    (activity as? AppCompatActivity)?.supportActionBar?.title =
                        "${candidate.firstName} ${candidate.surName}"
                    binding.detailCandidateNote.text = candidate.note
                    binding.detailCandidateDateOfBirth.text = candidate.dateOfBirth.toString()
                    binding.detailCandidateExpectedSalaryEuros.text =
                        candidate.expectedSalaryEuros.toString()

                    isFavorite = candidate.favorite
                    updateFavoriteIcon()
                    /*
                    binding.detailCandidateExpectedSalaryPounds.text =
                        candidate.expectedSalaryPounds*/

                    // Display the calculated age for this candidate, fetched from the ViewModel.
                    val candidateAge = detailCandidateViewModel.candidatesWithAge.value
                        .find { it.candidate.id == candidate.id }?.age ?: getString(R.string.age_unknown)
                    binding.detailCandidateAge.text = getString(R.string.year, candidateAge)
                }


                // Display errors, if any.
                if (uiState.error.isNotBlank()) {
                    Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                    // Reset the error state
                    detailCandidateViewModel.updateErrorState("")
                }

                //Observe if the candidate has been deleted
                if (uiState.isDeleted) {
                    // Show a success message.
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.delete_candidate),
                        Toast.LENGTH_SHORT
                    ).show()
                    // Return to the list of candidates or close this fragment.
                    requireActivity().onBackPressed()
                }

            }


        }
        return binding.root
    }

    /**
     * Creates the options menu for candidate actions.
     *
     * @param menu Menu in which the items will be placed.
     * @param inflater Inflater to inflate the menu.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // links the candidate's actions menu.
        inflater.inflate(R.menu.menu_detail_candidate, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Handles menu item selections.
     *
     * @param item Selected menu item.
     * @return `true` if the item is handled, otherwise calls the default behavior.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_favorite -> {
                // Toggle favorite status.
                toggleFavorite()
                return true
            }

            R.id.menu_edit -> {
                // Manage edit action.
                navigateToAddCandidateFragment()
                return true
            }

            R.id.menu_delete -> {
                // Manage delete action.
                showDeleteConfirmationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Toggles the favorite status of the candidate. If the candidate is currently a favorite, they
     * are removed from favorites; if not, they are added. Updates the UI icon accordingly.
     */
    private fun toggleFavorite() {
        val candidate = detailCandidateViewModel.uiState.value.candidate
        candidate?.let {
            if (isFavorite) {
                // If already a favorite, remove from favorites
                viewLifecycleOwner.lifecycleScope.launch {
                    detailCandidateViewModel.deleteFavoriteCandidate(it)
                }
            } else {
                // If not a favorite, add to favorites
                viewLifecycleOwner.lifecycleScope.launch {
                    detailCandidateViewModel.addNewFavoriteCandidate(it)
                }
            }

            // Toggle the favorite status
            isFavorite = !isFavorite
            updateFavoriteIcon()  // Update the UI (icon color)
        }
    }

    /**
     * Updates the favorite icon in the menu to reflect the candidate's favorite status. Displays a
     * filled star if the candidate is a favorite, otherwise an empty star.
     */
    private fun updateFavoriteIcon() {
        val favoriteIcon = requireActivity().findViewById<ImageButton>(R.id.menu_favorite)
        if (isFavorite) {
            favoriteIcon.setImageResource(R.drawable.star_full)  // Replace with your filled star drawable
        } else {
            favoriteIcon.setImageResource(R.drawable.star)  // Replace with your empty star drawable
        }
    }

    /**
     * Displays a confirmation dialog for candidate deletion. If confirmed, triggers the deletion
     * of the candidate through the ViewModel.
     */
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_deletion))
            .setMessage(getString(R.string.question_delete_candidate))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                detailCandidateViewModel.uiState.value.candidate?.let { candidate ->
                    detailCandidateViewModel.deleteCandidate(candidate)
                }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun navigateToAddCandidateFragment() {
        val candidate = detailCandidateViewModel.uiState.value.candidate
        candidate?.let {
            val bundle = Bundle().apply {
                putLong("candidateId", it.id)
            }
            findNavController().navigate(R.id.action_detailCandidateFragment_to_addCandidateFragment, bundle)
        }

    }


    companion object {
        private const val ARG_CANDIDATE_ID = "candidate_id"

        /**
         * Creates an instance of the fragment with the candidate ID passed as an argument.
         *
         * @param candidateId ID of the candidate to display.
         * @return A new instance of `DetailCandidateFragment` with the candidate ID.
         */
        fun newInstance(candidateId: Long): DetailCandidateFragment {
            val fragment = DetailCandidateFragment()
            val args = Bundle()
            args.putLong(ARG_CANDIDATE_ID, candidateId)
            fragment.arguments = args
            return fragment
        }
    }
}