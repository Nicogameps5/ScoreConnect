package com.example.scoreconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.R
import com.example.scoreconnect.model.MatchResult

class MatchResultAdapter(
    private val items: MutableList<MatchResult>,
    private val onJoin: (MatchResult) -> Unit
) : RecyclerView.Adapter<MatchResultAdapter.MatchViewHolder>() {

    fun updateItems(newItems: List<MatchResult>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_result, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sport: TextView = itemView.findViewById(R.id.tvSport)
        private val level: TextView = itemView.findViewById(R.id.tvLevel)
        private val date: TextView = itemView.findViewById(R.id.tvDate)
        private val location: TextView = itemView.findViewById(R.id.tvLocation)
        private val positionNeeded: TextView = itemView.findViewById(R.id.tvPositionNeeded)
        private val occupation: TextView = itemView.findViewById(R.id.tvOccupation)
        private val joinButton: Button = itemView.findViewById(R.id.btnJoinMatch)

        fun bind(match: MatchResult) {
            sport.text = match.sport
            level.text = "Level: ${match.level}"
            date.text = "Date: ${match.date}"
            location.text = "Location: ${match.location}"
            positionNeeded.text = "Needed positions: ${match.positionNeeded}"
            occupation.text = "Occupation: ${match.occupation}"
            joinButton.setOnClickListener { onJoin(match) }
        }
    }
}