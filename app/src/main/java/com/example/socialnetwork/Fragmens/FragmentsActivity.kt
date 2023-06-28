package com.example.socialnetwork.Fragmens

import ProfileFragment
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.socialnetwork.Fragmens.*
import com.example.socialnetwork.Fragments.DashboardFragment
import com.example.socialnetwork.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class FragmentsActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    val myId = FirebaseAuth.getInstance().currentUser?.uid

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        bottomNavigationView = findViewById(R.id.bottomMenu)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    // Implementacija za Home
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ProfileFragment())
                        .commit()
                    true
                }
                R.id.dashboard -> {
                    // Implementacija za Search
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, DashboardFragment())
                        .commit()
                    true
                }
                R.id.chat -> {
                    // Implementacija za Notifications
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ChatFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, DashboardFragment())
            .commit()
    }

    fun checkStatus(status : String){
        val map: HashMap<String, Any> = hashMapOf("status" to status)
        val reference = myId?.let { FirebaseDatabase.getInstance().getReference("myUsers").child(it) }
        reference?.updateChildren(map)
    }

    override fun onPostResume() {
        super.onPostResume()
        checkStatus("online")
    }

    override fun onPause() {
        super.onPause()
        checkStatus("offline")
    }

}
