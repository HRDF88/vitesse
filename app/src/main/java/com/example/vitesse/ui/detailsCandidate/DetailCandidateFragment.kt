package com.example.vitesse.ui.detailsCandidate

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.BitmapFactory
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
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

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
        // Retrieve the candidate ID from the fragment arguments
        candidateId = arguments?.getLong(ARG_CANDIDATE_ID)
        candidateId?.let {
            // Load candidate details if the ID is available
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

        // Setup actions for phone call, SMS, and email buttons
        binding.detailCandidateCall.setOnClickListener {
            val candidate = detailCandidateViewModel.uiState.value.candidate
            val phoneNumber = candidate?.phoneNumbers
            phoneNumber?.let { phone ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                startActivity(intent) // Open the phone app
            }
        }

        binding.detailCandidateSms.setOnClickListener {
            val candidate = detailCandidateViewModel.uiState.value.candidate
            val phoneNumber = candidate?.phoneNumbers
            phoneNumber?.let { phone ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone"))
                startActivity(intent) // Open the SMS app
            }
        }

        binding.detailCandidateEmail.setOnClickListener {
            val candidate = detailCandidateViewModel.uiState.value.candidate
            val email = candidate?.email
            email?.let { mail ->
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$mail"))
                intent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
                intent.putExtra(Intent.EXTRA_TEXT, "Body of the email")
                startActivity(intent) // Open the email app
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adjustFragmentContainerViewLayout(true) // Adjust layout on resume
        candidateId?.let {
            // Reload candidate details when fragment is resumed
            detailCandidateViewModel.loadCandidateDetails(it)
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.lifecycleScope.coroutineContext.cancelChildren() // Cancel all coroutines on view destruction
        super.onDestroyView()
    }

    /**
     * Sets up the toolbar with back navigation functionality.
     */
    private fun setupToolbar() {
        val toolbar: Toolbar = binding.root.findViewById(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar) // Set the toolbar as the ActionBar

        // Set the back icon and its click listener
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            // Adjust the fragment container layout before navigating back
            adjustFragmentContainerViewLayout(false)
            parentFragmentManager.popBackStack()
        }
    }

    /**
     * Adjusts the layout dimensions of the FragmentContainerView based on fragment state.
     *
     * @param isDetailFragment Whether this fragment is the detail fragment (to set full height).
     */
    private fun adjustFragmentContainerViewLayout(isDetailFragment: Boolean) {
        val fragmentContainerView =
            requireActivity().findViewById<FragmentContainerView>(R.id.main_view)
        val layoutParams = fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.height = if (isDetailFragment) {
            ConstraintLayout.LayoutParams.MATCH_PARENT
        } else {
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        }
        fragmentContainerView.layoutParams = layoutParams
    }

    /**
     * Sets up the options menu for the fragment, including the favorite and edit/delete actions.
     */
    private fun setupMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_detail_candidate, menu) // Inflate the menu
                // Initialize the favorite menu item
                favoriteMenuItem = menu.findItem(R.id.menu_favorite)
                updateFavoriteMenuIcon() // Update the favorite icon
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_favorite -> {
                        toggleFavorite() // Toggle favorite status
                        true
                    }
                    R.id.menu_edit -> {
                        navigateToAddCandidateFragment() // Navigate to edit candidate fragment
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

    /**
     * Observes candidate details from the ViewModel and updates the UI accordingly.
     */
    private fun observeCandidateDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            detailCandidateViewModel.uiState.collect { uiState ->
                // Show or hide the progress bar based on loading state
                binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE

                // If candidate data is available, update the UI
                uiState.candidate?.let { candidate ->
                    // Update the ActionBar title with the candidate's name
                    (activity as? AppCompatActivity)?.supportActionBar?.title =
                        "${candidate.firstName} ${candidate.surName}"

                    // Display the candidate's age or show "Age Unknown" if not available
                    val ageText = uiState.age?.let { getString(R.string.year, it) }
                        ?: getString(R.string.age_unknown)
                    binding.detailCandidateAge.text = ageText

                    // Update other candidate details in the UI
                    updateCandidateDetails(candidate)

                    // Display expected salary in pounds
                    binding.detailCandidateExpectedSalaryPounds.text = " ${uiState.expectedSalaryPounds}"

                    // Display the profile picture if it exists
                    if (candidate.profilePicture != null && candidate.profilePicture.isNotEmpty()) {
                        // Convert the profile picture (ByteArray) to Bitmap
                        val bitmap = BitmapFactory.decodeByteArray(candidate.profilePicture, 0, candidate.profilePicture.size)
                        binding.detailCandidateProfilePicture.setImageBitmap(bitmap)
                    } else {
                        // Set a placeholder if no profile picture is available
                        binding.detailCandidateProfilePicture.setImageResource(R.drawable.photo_camera)
                    }
                }

                // If there's an error message, display it
                if (uiState.error.isNotBlank()) {
                    Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                    detailCandidateViewModel.updateErrorState() // Reset the error state
                }

                // If candidate has been deleted, show a success message and navigate back
                if (uiState.isDeleted) {
                    showDeletionSuccess()
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    /**
     * Updates the UI with the candidate's details.
     *
     * @param candidate The candidate whose details need to be displayed.
     */
    private fun updateCandidateDetails(candidate: Candidate) {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Format and display the candidate's date of birth
        val formattedDateOfBirth = candidate.dateOfBirth.format(dateFormatter)
        binding.detailCandidateDateOfBirth.text = formattedDateOfBirth

        // Display other candidate details
        binding.detailCandidateNote.text = candidate.note
        binding.detailCandidateExpectedSalaryEuros.text = "${candidate.expectedSalaryEuros} €"

        // Update the favorite status and menu icon
        isFavorite = candidate.favorite
        updateFavoriteMenuIcon()
    }

    /**
     * Toggles the candidate's favorite status and updates the menu icon.
     */
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
            updateFavoriteMenuIcon() // Update the favorite icon in the menu
        }
    }

    /**
     * Updates the favorite icon in the options menu.
     */
    private fun updateFavoriteMenuIcon() {
        // Check if the favorite menu item has been initialized
        if (::favoriteMenuItem.isInitialized) {
            favoriteMenuItem.icon = ContextCompat.getDrawable(
                requireContext(),
                if (isFavorite) R.drawable.star_full else R.drawable.star
            )
        } else {
            Log.e(TAG, "favoriteMenuItem is not initialized")
        }
    }

    /**
     * Displays a confirmation dialog before deleting the candidate.
     */
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

    /**
     * Displays a success message after a candidate is deleted.
     */
    private fun showDeletionSuccess() {
        Toast.makeText(requireContext(), getString(R.string.delete_candidate), Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Navigates to the Add/Edit candidate fragment.
     */
    private fun navigateToAddCandidateFragment() {
        val candidate = detailCandidateViewModel.uiState.value.candidate
        candidate?.let {
            val addCandidateFragment = AddCandidateFragment.newInstance(it.id) // Pass candidate ID
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_view, addCandidateFragment) // Replace current fragment
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        private const val ARG_CANDIDATE_ID = "candidate_id"

        /**
         * Creates a new instance of the fragment with the given candidate ID.
         *
         * @param candidateId The ID of the candidate to display.
         * @return A new instance of `DetailCandidateFragment`.
         */
        fun newInstance(candidateId: Long): DetailCandidateFragment {
            return DetailCandidateFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CANDIDATE_ID, candidateId)
                }
            }
        }
    }
}
