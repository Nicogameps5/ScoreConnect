package com.example.scoreconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scoreconnect.R
import com.example.scoreconnect.model.User

class FriendUserAdapter(
    private val items: MutableList<User>,
    private val primaryButtonText: String?,
    private val onPrimaryClick: ((User) -> Unit)?,
    private val onViewProfile: (User) -> Unit
) : RecyclerView.Adapter<FriendUserAdapter.FriendUserViewHolder>() {

    fun updateItems(newItems: List<User>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_user, parent, false)
        return FriendUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendUserViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class FriendUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.tvFriendUsername)
        private val description: TextView = itemView.findViewById(R.id.tvFriendDescription)
        private val sports: TextView = itemView.findViewById(R.id.tvFriendSports)
        private val viewProfileButton: Button = itemView.findViewById(R.id.btnViewFriendProfile)
        private val primaryButton: Button = itemView.findViewById(R.id.btnFriendPrimaryAction)

        fun bind(user: User) {
            username.text = if (user.username.isBlank()) "Unknown user" else user.username
            description.text = if (user.description.isBlank()) "No description" else user.description

            sports.text = if (user.sports.isEmpty()) {
                "Sports: No sports selected"
            } else {
                "Sports: ${user.sports.joinToString(", ")}"
            }

            viewProfileButton.setOnClickListener {
                onViewProfile(user)
            }

            if (primaryButtonText == null || onPrimaryClick == null) {
                primaryButton.visibility = View.GONE
            } else {
                primaryButton.visibility = View.VISIBLE
                primaryButton.text = primaryButtonText
                primaryButton.setOnClickListener {
                    onPrimaryClick.invoke(user)
                }
            }
        }
    }
}