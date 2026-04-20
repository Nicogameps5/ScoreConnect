package com.example.scoreconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.R
import com.example.scoreconnect.model.WaitingPlayer

class WaitingPlayerAdapter(
    private val items: MutableList<WaitingPlayer>,
    private val onAccept: (WaitingPlayer) -> Unit,
    private val onReject: (WaitingPlayer) -> Unit
) : RecyclerView.Adapter<WaitingPlayerAdapter.WaitingPlayerViewHolder>() {

    fun updateItems(newItems: List<WaitingPlayer>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaitingPlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_waiting_player, parent, false)
        return WaitingPlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaitingPlayerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class WaitingPlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val position: TextView = itemView.findViewById(R.id.tvPosition)
        private val name: TextView = itemView.findViewById(R.id.tvPlayerName)
        private val acceptButton: Button = itemView.findViewById(R.id.btnAccept)
        private val rejectButton: Button = itemView.findViewById(R.id.btnReject)

        fun bind(player: WaitingPlayer) {
            position.text = player.position
            name.text = player.name
            acceptButton.setOnClickListener { onAccept(player) }
            rejectButton.setOnClickListener { onReject(player) }
        }
    }
}