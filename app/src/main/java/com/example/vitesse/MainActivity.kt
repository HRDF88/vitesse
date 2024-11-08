package com.example.vitesse

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.vitesse.ui.addCandidate.AddCandidateFragment
import com.example.vitesse.ui.home.ViewPagerAdapter
import com.example.vitesse.ui.interfaceUi.FilterableInterface
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
/**
 * Main activity that hosts the fragments and manages the user interface interactions,
 * including a ViewPager2 for tab navigation, a FloatingActionButton for adding candidates,
 * and a SearchView for filtering candidates in the active fragment.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var searchView: SearchView
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    /**
     * Called when the activity is created.
     * Initializes the ViewPager, SearchView, and FloatingActionButton.
     * Sets up TabLayout and ViewPager2 for tab navigation.
     * Adds click listeners for the FloatingActionButton and SearchView.
     *
     * @param savedInstanceState The saved instance state of the activity, if available.
     */
    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views and components
        viewPager = findViewById(R.id.viewPager2)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        searchView = findViewById(R.id.searchView)
        val fab = findViewById<FloatingActionButton>(R.id.main_add_candidate_button)

        // Set up ViewPager with adapter to manage the fragments in tabs
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        // Connect TabLayout and ViewPager, setting the tab names
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) getString(R.string.all) else getString(R.string.favorite)
        }.attach()

        // Floating Action Button Click Listener to open AddCandidateFragment.
        fab.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_view, AddCandidateFragment())
                .addToBackStack(null)
                .commit()
        }

        // Add listener for back stack changes to hide/show the FAB based on the fragment.
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.main_view)
            fab.visibility =
                if (currentFragment is AddCandidateFragment) View.GONE else View.VISIBLE
        }

        // Customize SearchView and set up listener for text changes
        setupSearchView()

        // SearchView listener for filtering candidates based on text input
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            /**
             * Filters the candidates based on the query text change.
             * It checks the current fragment and applies the filter to the active fragment.
             *
             * @param newText The new text entered in the SearchView.
             * @return True to indicate that the query text has been handled.
             */
            override fun onQueryTextChange(newText: String?): Boolean {
                // Apply the filter to the active fragment
                val currentFragment =
                    supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
                if (currentFragment is FilterableInterface) {
                    currentFragment.filter(newText ?: "")
                }
                return true
            }
        })
    }

    /**
     * Set up the customizations for the SearchView.
     * Changes the text color, hint color, and the search icon color.
     */
    private fun setupSearchView() {
        // Access EditText inside the SearchView
        val searchEditText: EditText =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text)

        // Set text color to black
        searchEditText.setTextColor(ContextCompat.getColor(this, android.R.color.black))

        // Optionally, set hint color to black as well
        searchEditText.setHintTextColor(ContextCompat.getColor(this, android.R.color.black))

        // Optionally, change the compound drawable (search icon)
        val drawableRight = AppCompatResources.getDrawable(this, R.drawable.search)
        searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null)

        // Change search icon color to black
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
        searchIcon.setColorFilter(
            ContextCompat.getColor(this, android.R.color.black),
            PorterDuff.Mode.SRC_IN
        )
    }
}