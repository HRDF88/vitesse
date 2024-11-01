package com.example.vitesse.ui.home.candidate

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vitesse.R
import com.example.vitesse.domain.model.Candidate

class CandidateAdapter(private var candidate: List<Candidate>) :
    RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val itemView =
            android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_candidate, parent, false)
        return CandidateViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CandidateAdapter.CandidateViewHolder, position: Int) {
        val candidate = candidate[position]
        holder.tvFirstName.text = candidate.firstName
        holder.tvLastName.text = candidate.surName
        holder.tvNote.text = candidate.note

    }

    override fun getItemCount() = candidate.size

    inner class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvFirstName: TextView = itemView.findViewById(R.id.firstname)
        var tvLastName: TextView = itemView.findViewById(R.id.lastname)
        var tvNote: TextView = itemView.findViewById(R.id.note_candidate)
    }

    @SuppressLint("NotifyDatasetChanged")
    fun uptdateData(newCandidate: List<Candidate>) {
        this.candidate = newCandidate
        notifyDataSetChanged()
    }

}