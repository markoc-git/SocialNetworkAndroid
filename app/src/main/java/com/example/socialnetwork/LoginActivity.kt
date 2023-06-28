package com.example.socialnetwork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialnetwork.Fragmens.FragmentsActivity

import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null)
            startActivity(Intent(this, FragmentsActivity::class.java))
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val text = findViewById<TextView>(R.id.toRegistration)
        val emailInput = findViewById<EditText>(R.id.loginEmail)
        val passwordInput = findViewById<EditText>(R.id.loginPassword)
        val btnLogin =  findViewById<Button>(R.id.button_submit)

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if(email == "")
                emailInput.error

            if(password.length < 6)
                passwordInput.error = "Password must have 8 characters"

            login(email,password)

        }

        text.setOnClickListener {
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnSuccessListener {
            Toast.makeText(this,"Successful Log In",Toast.LENGTH_LONG).show()
            startActivity(Intent(this, FragmentsActivity::class.java))
        }.addOnFailureListener {
            Toast.makeText(this, "Inccorect Password or Email address", Toast.LENGTH_LONG).show()
        }
    }

}



