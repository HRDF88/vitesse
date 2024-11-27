package com.example.vitesse.ui.home.candidate

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate
import com.example.vitesse.ui.interfaceUi.FilterableInterface

/**
 * Adapter class for the RecyclerView that displays a list of all candidates.
 */
class CandidateAdapter(
    private var candidate: List<Candidate>,
    private val onItemClick: (Candidate) -> Unit
) :
    RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>(), FilterableInterface {

    private var filteredCandidates: List<Candidate> = candidate

    /**
     * Create View Holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val itemView =
            android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_candidate, parent, false)
        return CandidateViewHolder(itemView)
    }

    /**
     * Holder values of  elements on View Holder.
     */
    override fun onBindViewHolder(holder: CandidateAdapter.CandidateViewHolder, position: Int) {
        val candidate = filteredCandidates[position]
        holder.tvFirstName.text = candidate.firstName
        holder.tvLastName.text = candidate.surName
        holder.tvNote.text = candidate.note

        //Update accessibility description
        holder.itemView.contentDescription = "${candidate.firstName} ${candidate.surName}, note : ${candidate.note}"

        candidate.profilePicture?.let { byteArray ->
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            holder.profilePicture.setImageBitmap(bitmap)
        }

        // Set click listener on the item view
        holder.itemView.setOnClickListener {
            onItemClick(candidate) // Call the callback with the clicked candidate
        }
    }

    override fun getItemCount() = filteredCandidates.size

    /**
     * Binding XML elements of View Holder.
     */
    inner class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvFirstName: TextView = itemView.findViewById(R.id.firstname)
        var tvLastName: TextView = itemView.findViewById(R.id.lastname)
        var tvNote: TextView = itemView.findViewById(R.id.note_candidate)
        var profilePicture: ImageView = itemView.findViewById(R.id.profile_picture)
    }

    /**
     * Update DATA for recycler view.
     */
    @SuppressLint("NotifyDatasetChanged")
    fun updateData(newCandidate: List<Candidate>) {
        this.candidate = newCandidate
        this.filteredCandidates = newCandidate
        notifyDataSetChanged()
    }

    /**
     * Update filtered list based on search query.
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun filter(query: String) {
        filteredCandidates = if (query.isEmpty()) {
            candidate
        } else {
            candidate.filter {
                it.firstName.contains(query, ignoreCase = true) || it.surName.contains(
                    query,
                    ignoreCase = true
                )
            }
        }
        notifyDataSetChanged()
    }

}