package com.example.vitesse.ui.home.favoriteCandidate

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate

/**
 * Adapter class for the RecyclerView that displays a list of favorite candidates.
 */
class FavoriteCandidateAdapter(
    private var favoriteCandidate: List<Candidate> = emptyList(),
    private val onItemClick: (Candidate) -> Unit
) :
    RecyclerView.Adapter<FavoriteCandidateAdapter.FavoriteCandidateViewHolder>() {

    private var filteredFavoriteCandidates: List<Candidate> = favoriteCandidate


    /**
     * Create View Holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteCandidateViewHolder {
        val itemView =
            android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_favorite_candidate, parent, false)
        return FavoriteCandidateViewHolder(itemView)
    }

    /**
     * Holder values of  elements on View Holder.
     */
    override fun onBindViewHolder(
        holder: FavoriteCandidateAdapter.FavoriteCandidateViewHolder,
        position: Int
    ) {
        val favoriteCandidate = filteredFavoriteCandidates[position]

        holder.tvFirstName.text = favoriteCandidate.firstName
        holder.tvLastName.text = favoriteCandidate.surName
        holder.tvNote.text = favoriteCandidate.note

        holder.itemView.contentDescription = "${favoriteCandidate.firstName} ${favoriteCandidate.surName}, note : ${favoriteCandidate.note}"

        favoriteCandidate.profilePicture?.let { byteArray ->
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            holder.profilePicture.setImageBitmap(bitmap)
        }

        // Set click listener on the item view
        holder.itemView.setOnClickListener {
            onItemClick(favoriteCandidate)
        }

    }

    /**
     * Gets item size to display.
     */
    override fun getItemCount() = filteredFavoriteCandidates.size

    /**
     * Binding XML elements of View Holder.
     */
    inner class FavoriteCandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvFirstName: TextView = itemView.findViewById(R.id.favorite_firstname)
        var tvLastName: TextView = itemView.findViewById(R.id.favorite_lastname)
        var tvNote: TextView = itemView.findViewById(R.id.favorite_note_candidate)
        var profilePicture: ImageView = itemView.findViewById(R.id.favorite_profile_picture)
    }

    /**
     * Update DATA for recycler view.
     */
    @SuppressLint("NotifyDatasetChanged")
    fun updateData(newFavoriteCandidate: List<Candidate>) {
        favoriteCandidate = newFavoriteCandidate
        filteredFavoriteCandidates = newFavoriteCandidate
        notifyDataSetChanged()
    }

    /**
     * Update the filtered list based on search query.
     */
    fun filter(query: String) {
        filteredFavoriteCandidates = if (query.isEmpty()) {
            favoriteCandidate
        } else {
            favoriteCandidate.filter {
                it.firstName.contains(query, ignoreCase = true) || it.surName.contains(
                    query,
                    ignoreCase = true
                )
            }
        }
        notifyDataSetChanged()
    }
}
