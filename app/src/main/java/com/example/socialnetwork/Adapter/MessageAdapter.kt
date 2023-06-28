package com.example.socialnetwork.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.Modal.Chat
import com.example.socialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class MessageAdapter(private val context: Context, private val chatList: List<Chat>, private val imgUrl: String) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val MSG_TYPE_LEFT = 0
    private val MSG_TYPE_RIGHT = 1
    private val fuser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = if (viewType == MSG_TYPE_RIGHT) {
            LayoutInflater.from(context).inflate(R.layout.left_side, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.right_side, parent, false)
        }
        return MessageViewHolder(view)
    }


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val chat = chatList[position]

        holder.show_message.text = chat.message

        if (imgUrl == "default")
            holder.profile_image.setImageResource(R.mipmap.ic_launcher)
         else
            Picasso.get()
                .load(imgUrl)
                .into(holder.profile_image)

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].sender == fuser?.uid)
            MSG_TYPE_RIGHT
         else
            MSG_TYPE_LEFT

    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile_image: ImageView = itemView.findViewById(R.id.profile_image)
        var show_message: TextView = itemView.findViewById(R.id.show_message)
    }
}
