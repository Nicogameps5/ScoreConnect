package com.example.scoreconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.R
import com.example.scoreconnect.model.FriendRequest

class FriendRequestAdapter(
    private val items: MutableList<FriendRequest>,
    private val onAccept: (FriendRequest) -> Unit,
    private val onReject: (FriendRequest) -> Unit,
    private val onViewProfile: (FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>() {

    fun updateItems(newItems: List<FriendRequest>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.tvRequestUsername)
        private val info: TextView = itemView.findViewById(R.id.tvRequestInfo)
        private val viewProfileButton: Button = itemView.findViewById(R.id.btnViewRequestProfile)
        private val acceptButton: Button = itemView.findViewById(R.id.btnAcceptRequest)
        private val rejectButton: Button = itemView.findViewById(R.id.btnRejectRequest)

        fun bind(request: FriendRequest) {
            username.text = request.senderUsername
            info.text = "Wants to be your friend"

            viewProfileButton.setOnClickListener {
                onViewProfile(request)
            }

            acceptButton.setOnClickListener {
                onAccept(request)
            }

            rejectButton.setOnClickListener {
                onReject(request)
            }
        }
    }
}