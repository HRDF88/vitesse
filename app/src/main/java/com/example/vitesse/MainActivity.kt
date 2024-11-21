package com.example.vitesse

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.viewpager2.widget.ViewPager2
import com.example.vitesse.ui.addCandidate.AddCandidateFragment
import com.example.vitesse.ui.detailsCandidate.DetailCandidateFragment
import com.example.vitesse.ui.home.ViewPagerAdapter
import com.example.vitesse.ui.interfaceUi.FilterableInterface
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Declare variables for components
    private lateinit var viewPager: ViewPager2
    private lateinit var searchView: SearchView
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var fragmentContainerView: FragmentContainerView

    // Request code for permissions
    private val PERMISSIONS_REQUEST_CODE = 1001

    /**
     * Called when the activity is created.
     * Initializes the components, sets up the view, and assigns listeners for various actions.
     */
    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragmentContainerView = findViewById(R.id.main_view)


        // Initialize views
        viewPager = findViewById(R.id.viewPager2)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        searchView = findViewById(R.id.searchView)
        val fab = findViewById<FloatingActionButton>(R.id.main_add_candidate_button)

        // Set up the ViewPager with an adapter to manage fragments within the tabs
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        // Link TabLayout with ViewPager and set tab names dynamically
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) getString(R.string.all) else getString(R.string.favorite)
        }.attach()

        // Set up Floating Action Button (FAB) click listener to navigate to AddCandidateFragment
        fab.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_view, AddCandidateFragment())
                .addToBackStack(null)
                .commit()
        }

        // Handle changes in the back stack to show/hide the FAB based on the fragment displayed
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.main_view)

            fab.visibility = when (currentFragment) {
                is AddCandidateFragment, is DetailCandidateFragment -> View.GONE // Cache le FAB pour les fragments de détail ou d'ajout
                else -> View.VISIBLE // Affiche le FAB pour les autres fragments
            }
        }

        // Set up SearchView to filter candidates based on the input text
        setupSearchView()

        // SearchView listener to handle text input and filter candidates accordingly
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                // Apply filter to the active fragment based on the input text
                val currentFragment =
                    supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
                if (currentFragment is FilterableInterface) {
                    currentFragment.filter(newText ?: "")
                }
                return true
            }
        })

        // Request necessary permissions at runtime
        requestPermissionsIfNeeded()
    }
    override fun onResume() {
        super.onResume()
        adjustFragmentContainerViewLayout(false)
    }
    /**
     * Checks and requests necessary permissions at runtime.
     * Handles permissions for reading and writing to external storage or media files.
     */
    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf<String>()

        // Check if the app has storage permissions based on Android version
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) { // For Android 10 and below (API 29)
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // For Android 11 and above (API 30)
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // For Android 13 (API 33)
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }

        // Request permissions if they are not granted
        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }


    /**
     * Handles the result of the permission request.
     * Displays a toast message depending on whether the permission was granted or denied.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this, "Permission granted for ${permissions[i]}", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission denied for ${permissions[i]}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    /**
     * Adjusts the dimensions of the FragmentContainerView according to the displayed fragment.
     */
    fun adjustFragmentContainerViewLayout(isDetailFragment: Boolean) {
        val layoutParams = fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
        if (isDetailFragment) {
            // Prendre toute la hauteur de l'écran
            layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
        } else {
            // Réinitialiser la hauteur à wrap_content (par défaut)
            layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

        }
        fragmentContainerView.layoutParams = layoutParams
    }
    /**
     * Customizes the SearchView component for better usability.
     * Changes text color, hint color, and search icon color.
     */
    private fun setupSearchView() {
        // Access the EditText inside the SearchView
        val searchEditText: EditText =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text)

        // Set text color to black
        searchEditText.setTextColor(ContextCompat.getColor(this, android.R.color.black))

        // Set hint color to black as well
        searchEditText.setHintTextColor(ContextCompat.getColor(this, android.R.color.black))

        // Change the search icon color to black
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
        searchIcon.setColorFilter(
            ContextCompat.getColor(this, android.R.color.black),
            PorterDuff.Mode.SRC_IN
        )
    }
}
