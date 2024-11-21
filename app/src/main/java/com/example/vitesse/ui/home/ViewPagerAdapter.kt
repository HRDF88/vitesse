package com.example.vitesse.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.vitesse.ui.home.candidate.CandidateFragment
import com.example.vitesse.ui.home.favoriteCandidate.FavoriteCandidateFragment

/**
 * A custom adapter for managing fragments in a ViewPager. This adapter handles the
 * creation and management of fragments for the ViewPager, ensuring that the correct
 * fragment is displayed based on the selected position.
 *
 * @param activity The [FragmentActivity] that hosts the ViewPager. This is passed to the
 *                 [FragmentStateAdapter] constructor to manage the fragment lifecycle.
 */
class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    /**
     * Returns the total number of fragments that the adapter will manage.
     *
     * @return The total number of fragments (2 in this case, representing the Candidate
     *         and FavoriteCandidate fragments).
     */
    override fun getItemCount(): Int = 2

    /**
     * Creates and returns the appropriate fragment for the given position in the ViewPager.
     *
     * @param position The position of the fragment to be created. It determines which fragment
     *                 to instantiate:
     *                 - Position 0 corresponds to [CandidateFragment].
     *                 - Position 1 corresponds to [FavoriteCandidateFragment].
     *
     * @return The fragment corresponding to the provided position.
     * @throws IllegalStateException If the position is not 0 or 1.
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CandidateFragment()
            1 -> FavoriteCandidateFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}