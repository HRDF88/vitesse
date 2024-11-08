package com.example.vitesse

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.vitesse.ui.home.ViewPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.vitesse.ui.addCandidate.AddCandidateFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager2)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val fab = findViewById<FloatingActionButton>(R.id.main_add_candidate_button)

        // Set up ViewPager with Adapter
        viewPager.adapter = ViewPagerAdapter(this)

        // Connect TabLayout and ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text =
                if (position == 0) getString(R.string.all) else getString(R.string.favorite)
        }.attach()


        // Floating Action Button Click Listener to open add_candidate_fragment.
        fab.setOnClickListener {
            Log.d("MainActivity", "FAB clicked, attempting to replace fragment.")
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_view, AddCandidateFragment())
                .addToBackStack(null) // Ajoute le fragment à la pile de retour pour pouvoir revenir en arrière
                .commit()
            Log.d("MainActivity", "Fragment transaction committed.")
            val fragment =
                supportFragmentManager.findFragmentByTag(AddCandidateFragment::class.java.simpleName)
            if (fragment != null && fragment.isVisible) {
                Log.d("MainActivity", "Fragment is visible.")
            } else {
                Log.d("MainActivity", "Fragment is not visible.")
            }
        }

// Accéder à l'EditText interne de SearchView
        val searchEditText: EditText =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text)

// Ajouter une icône à droite de l'EditText interne
        val drawableRight =
            resources.getDrawable(R.drawable.search)
        searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null)

// Accéder à l'icône de recherche interne du SearchView
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)

        // Changer la couleur de l'icône en noir
        searchIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_IN)



// Si vous souhaitez gérer un clic sur l'icône
        searchEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (searchEditText.right - searchEditText.compoundDrawables[2].bounds.width())) {
                    // L'icône a été cliquée
                    val query = searchEditText.text.toString()
                    // Effectuer une action avec la recherche, par exemple afficher un Toast
                    Toast.makeText(this, "Recherche : $query", Toast.LENGTH_SHORT).show()
                    return@setOnTouchListener true
                }
            }
            false
        }


    }
}


