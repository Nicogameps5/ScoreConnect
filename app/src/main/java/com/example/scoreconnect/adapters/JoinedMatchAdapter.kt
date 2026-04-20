package com.example.scoreconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.R
import com.example.scoreconnect.model.JoinedMatch

class JoinedMatchAdapter(
    private val items: MutableList<JoinedMatch>
) : RecyclerView.Adapter<JoinedMatchAdapter.JoinedMatchViewHolder>() {

    fun updateItems(newItems: List<JoinedMatch>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoinedMatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_joined_match, parent, false)
        return JoinedMatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: JoinedMatchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class JoinedMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sport: TextView = itemView.findViewById(R.id.tvJoinedSport)
        private val date: TextView = itemView.findViewById(R.id.tvJoinedDate)
        private val location: TextView = itemView.findViewById(R.id.tvJoinedLocation)
        private val level: TextView = itemView.findViewById(R.id.tvJoinedLevel)
        private val occupation: TextView = itemView.findViewById(R.id.tvJoinedOccupation)
        private val position: TextView = itemView.findViewById(R.id.tvJoinedPosition)
        private val status: TextView = itemView.findViewById(R.id.tvJoinedStatus)

        fun bind(match: JoinedMatch) {
            sport.text = match.sport
            date.text = "Date: ${match.dateTime}"
            location.text = "Location: ${match.location}"
            level.text = "Level: ${match.level}"
            occupation.text = "Occupation: ${match.currentPlayers}/${match.totalPlayers} players"
            position.text = "Requested position: ${match.requestedPosition}"

            status.text = when (match.requestStatus) {
                "accepted" -> "Status: Accepted"
                "pending" -> "Status: Pending"
                "rejected" -> "Status: Rejected"
                else -> "Status: ${match.requestStatus}"
            }
        }
    }
}