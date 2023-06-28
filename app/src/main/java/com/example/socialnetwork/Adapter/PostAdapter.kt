package com.example.socialnetwork.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.Modal.Post
import com.example.socialnetwork.Modal.User
import com.example.socialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val context: Context, private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postList[position]
        val db = FirebaseDatabase.getInstance().reference

        db.child("myUsers").child(post.userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)
                user?.let {
                    // Prikazivanje imena korisnika
                    holder.txtUsername.text = user.username

                    // Učitavanje i prikazivanje profilne slike korisnika
                    if (it.imgUrl == "default") {
                        holder.imgProfile.setImageResource(R.mipmap.ic_launcher)
                    } else {
                        Picasso.get().load(it.imgUrl).into(holder.imgProfile)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obrada greške
            }
        })

        db.child("images").child(post.postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postSnapshot: DataSnapshot = snapshot.child("likes")
                val likes = postSnapshot.getValue(Int::class.java)
                holder.likes.text = likes.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Obrada greške
            }
        })

        Picasso.get().load(post.postUrl).into(holder.imgPost)

        holder.bind(post)

    }

    override fun getItemCount(): Int {
        return postList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtUsername: TextView = itemView.findViewById(R.id.txtUsernames)
        var imgProfile: CircleImageView = itemView.findViewById(R.id.imgProfile)
        var imgPost: ImageView = itemView.findViewById(R.id.imgPosts)
        var toggle: ToggleButton = itemView.findViewById(R.id.toggleButton)
        var likes: TextView = itemView.findViewById(R.id.txtLikes)

        fun bind(post: Post) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val likesRef = FirebaseDatabase.getInstance().reference
                .child("images")
                .child(post.postId)

            // Provera da li korisnik lajkuje objavu i postavljanje odgovarajućeg stanja ToggleButton-a
            currentUserId?.let {
                likesRef.child("likedByUsers").child(it)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val isLiked = snapshot.exists()
                            toggle.isChecked = isLiked || post.isLiked // Postavljamo stanje ToggleButton-a na osnovu isLiked svojstva posta
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Obrada greške
                        }
                    })
            }

            toggle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Dodavanje korisnikovog ID-a u listu lajkova za post
                    currentUserId?.let { likesRef.child("likedByUsers").child(it).setValue(true) }

                    // Povećanje broja lajkova za 1
                    likesRef.child("likes")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val likes = snapshot.getValue(Int::class.java) ?: 0
                                likesRef.child("likes").setValue(likes + 1)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Obrada greške
                            }
                        })
                } else {
                    // Uklanjanje korisnikovog ID-a iz liste lajkova za post
                    currentUserId?.let { likesRef.child("likedByUsers").child(it).removeValue() }

                    // Smanjenje broja lajkova za 1 ako je broj lajkova veći od 0
                    likesRef.child("likes")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val likes = snapshot.getValue(Int::class.java) ?: 0
                                if (likes > 0) {
                                    likesRef.child("likes").setValue(likes - 1)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Obrada greške
                            }
                        })
                }
            }
        }}

}
