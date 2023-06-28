package com.example.socialnetwork

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.socialnetwork.Modal.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


class EditProfile : AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var newNameE: EditText
    private lateinit var newEmailE: EditText
    private lateinit var newPasswordE: EditText
    private lateinit var reference: DatabaseReference
    private lateinit var cuser: FirebaseUser
    private lateinit var editBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        init()
        title = "Edit Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                profileName.text = user?.username

                if (user?.imgUrl == "default") {
                    profileImage.setImageResource(R.mipmap.ic_launcher)
                } else {
                    Picasso.Builder(applicationContext)
                        .build()
                        .load(user?.imgUrl)
                        .into(profileImage)
                }

                newNameE.setText(user?.username)
                newEmailE.setText(user?.email)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancelled event
            }
        })

        editBtn.setOnClickListener {
            val newName = newNameE.text.toString()
            val newEmail = newEmailE.text.toString()
            val newPassword = newPasswordE.text.toString()

            if (!newName.isEmpty() && newName.length >= 3) {
                val map: HashMap<String, Any> = HashMap()
                map["username"] = newName
                reference.updateChildren(map)
            } else {
                newNameE.setError("Name must have min 3 letters")
            }

            if (!newEmail.isEmpty()) {
                cuser.updateEmail(newEmail).addOnCompleteListener { e ->
                    if (e.isSuccessful) {
                        val map: HashMap<String, Any> = HashMap()
                        map["email"] = newEmail
                        reference.updateChildren(map)
                    }
                }
            } else {
                newEmailE.setError("Failed email address")
            }

            if (newPassword.length >= 6) {
                cuser.updatePassword(newPassword).addOnCompleteListener { task ->
                    Toast.makeText(
                        this@EditProfile,
                        "Successful changed password",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this@EditProfile,
                        "An error has occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }

    private fun init() {
        profileName = findViewById(R.id.eprofile_name)
        profileImage = findViewById(R.id.profile_eeimage)
        newEmailE = findViewById(R.id.et_email)
        newPasswordE = findViewById(R.id.et_password)
        newNameE = findViewById(R.id.et_name)
        cuser = FirebaseAuth.getInstance().currentUser!!
        reference =
            FirebaseDatabase.getInstance().getReference("myUsers").child(cuser.uid)
        editBtn = findViewById(R.id.btn_edit)
    }

    private fun checkStatus(status: String) {
        val map: HashMap<String, Any> = HashMap()
        map["status"] = status
        reference.updateChildren(map)
    }

    override fun onResume() {
        super.onResume()
        checkStatus("online")
    }

    override fun onPause() {
        super.onPause()
        checkStatus("offline")
    }
}
