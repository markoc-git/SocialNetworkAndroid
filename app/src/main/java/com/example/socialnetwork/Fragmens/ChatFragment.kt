package com.example.socialnetwork.Fragmens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.Adapter.UsersAdapter
import com.example.socialnetwork.Modal.User
import com.example.socialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {
    private lateinit var recycle: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var searchView: SearchView
    private val myId = FirebaseAuth.getInstance().currentUser?.uid
    private val users = mutableListOf<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recycle = view.findViewById(R.id.recycle)
        searchView = view.findViewById(R.id.searchView)

        setupRecyclerView()
        readUsers()



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterUsers(newText)
                return true
            }
        })

        return view
    }

    private fun setupRecyclerView() {
        usersAdapter = UsersAdapter(users, requireContext())
        recycle.adapter = usersAdapter
        recycle.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun filterUsers(query: String) {
        val filteredUsers = mutableListOf<User>()

        for (user in users) {
            if (user.username.contains(query, ignoreCase = true)) {
                filteredUsers.add(user)
            }
        }

        usersAdapter.setData(filteredUsers)
    }

    private fun readUsers() {
        FirebaseDatabase.getInstance().getReference("myUsers")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    users.clear()

                    for (snapshot1 in snapshot.children) {
                        val user = snapshot1.getValue(User::class.java)
                        if (user != null && user.id != myId) {
                            users.add(user)
                        }
                    }
                    usersAdapter.setData(users)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error " + error.message, Toast.LENGTH_LONG).show()
                }
            })
    }


}
