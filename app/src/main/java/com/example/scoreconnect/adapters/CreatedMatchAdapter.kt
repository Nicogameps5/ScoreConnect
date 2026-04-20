package com.example.scoreconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.R
import com.example.scoreconnect.model.CreatedMatch

class CreatedMatchAdapter(
    private val items: MutableList<CreatedMatch>,
    private val onManageClick: (CreatedMatch) -> Unit
) : RecyclerView.Adapter<CreatedMatchAdapter.CreatedMatchViewHolder>() {

    fun updateItems(newItems: List<CreatedMatch>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatedMatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_created_match, parent, false)
        return CreatedMatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: CreatedMatchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CreatedMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sport: TextView = itemView.findViewById(R.id.tvHistorySport)
        private val date: TextView = itemView.findViewById(R.id.tvHistoryDate)
        private val location: TextView = itemView.findViewById(R.id.tvHistoryLocation)
        private val level: TextView = itemView.findViewById(R.id.tvHistoryLevel)
        private val occupation: TextView = itemView.findViewById(R.id.tvHistoryOccupation)
        private val pending: TextView = itemView.findViewById(R.id.tvHistoryPending)
        private val manageButton: Button = itemView.findViewById(R.id.btnManageMatch)

        fun bind(match: CreatedMatch) {
            sport.text = match.sport
            date.text = "Date: ${match.dateTime}"
            location.text = "Location: ${match.location}"
            level.text = "Level: ${match.level}"
            occupation.text = "Occupation: ${match.currentPlayers}/${match.totalPlayers} players"
            pending.text = "Pending requests: ${match.pendingRequests}"
            manageButton.setOnClickListener { onManageClick(match) }
        }
    }
}