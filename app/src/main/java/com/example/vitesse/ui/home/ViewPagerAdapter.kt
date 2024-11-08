package com.example.vitesse.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.vitesse.ui.home.candidate.CandidateFragment
import com.example.vitesse.ui.home.favoriteCandidate.FavoriteCandidateFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CandidateFragment()
            1 -> FavoriteCandidateFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}