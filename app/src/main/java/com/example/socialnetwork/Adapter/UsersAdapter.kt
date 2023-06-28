package com.example.socialnetwork.Adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.MessageActivity
import com.example.socialnetwork.Modal.User
import com.example.socialnetwork.R
import com.squareup.picasso.Picasso

class UsersAdapter(var users: MutableList<User>, val context :Context) : RecyclerView.Adapter<UserHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.users_list,parent,false)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int,) {
            val user = users[position]
            holder.title.text = user.username

            if(user.imgUrl == "default")
                holder.image.setImageResource(R.mipmap.ic_launcher)
            else
                Picasso.Builder(context).build().load(user.imgUrl).into(holder.image)

            if (user.status == "online") {
                holder.statusOff.visibility = View.GONE
                holder.statusOn.visibility = View.VISIBLE
            } else {
                holder.statusOff.visibility = View.VISIBLE
                holder.statusOn.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                val i = Intent(context, MessageActivity::class.java)
                i.putExtra("id",user.id)
                context.startActivity(i)
            }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun setData(users: List<User>) {
        this.users = users as MutableList<User>
        notifyDataSetChanged()
    }

}

class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title : TextView = itemView.findViewById(R.id.profileName)
    val image : ImageView = itemView.findViewById(R.id.profileImg)
    val statusOff : ImageView = itemView.findViewById(R.id.statusOff)
    val statusOn : ImageView = itemView.findViewById(R.id.status)
}