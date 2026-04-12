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
    private val items: List<MatchResult>,
    private val onJoin: (MatchResult) -> Unit
) : RecyclerView.Adapter<MatchResultAdapter.MatchViewHolder>() {

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
        private val price: TextView = itemView.findViewById(R.id.tvPrice)
        private val date: TextView = itemView.findViewById(R.id.tvDate)
        private val position: TextView = itemView.findViewById(R.id.tvPositionNeeded)
        private val location: TextView = itemView.findViewById(R.id.tvLocation)
        private val joinButton: Button = itemView.findViewById(R.id.btnJoinMatch)

        fun bind(match: MatchResult) {
            sport.text = match.sport
            level.text = "Level: ${match.level}"
            price.text = "Price: ${match.price}"
            date.text = "Date: ${match.date}"
            position.text = "Position needed: ${match.positionNeeded}"
            location.text = match.location
            joinButton.setOnClickListener { onJoin(match) }
        }
    }
}