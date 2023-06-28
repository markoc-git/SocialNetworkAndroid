package com.example.socialnetwork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.socialnetwork.Fragmens.FragmentsActivity
import com.example.socialnetwork.Modal.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private var db = FirebaseDatabase.getInstance().getReference("myUsers")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        val  toLogin = findViewById<TextView>(R.id.toLogin)
        val emailInput = findViewById<EditText>(R.id.editTextEmail)
        val usernameInput = findViewById<EditText>(R.id.editTextName)
        val registerButton = findViewById<Button>(R.id.registrationbtn)
        val passwordE = findViewById<EditText>(R.id.editTextPassword)
        val passwordRE = findViewById<EditText>(R.id.editTextPasswordR)

        registerButton.setOnClickListener{
           val email  = emailInput.text.toString()
           val username = usernameInput.text.toString()
           val password = passwordE.text.toString()
           val passwordR = passwordRE.text.toString()

           if(password != passwordR || password.length <  6|| passwordR.length < 6){
               passwordE.error = "passwords do not match or password don't have 8 characters "
               passwordRE.error = "passwords do not match or password don't have 8 characters "
           }

            if(username.length < 3 || username == "")
                usernameInput.error =  "Username must have 3 characters"

            if(email == "")
                emailInput.error = "Email field can't be empty"

           if(password == passwordR && username.length >= 3 && password.length >= 6 && email != "")
                registarNow(email,username,password)

        }

        toLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registarNow(email: String, username: String, password: String) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                val id = FirebaseAuth.getInstance().currentUser?.uid.toString()
                val user = User(username,email,"default","online",id)

                db.child(id).setValue(user)
                startActivity(Intent(this, FragmentsActivity::class.java))
        }
    }

}





