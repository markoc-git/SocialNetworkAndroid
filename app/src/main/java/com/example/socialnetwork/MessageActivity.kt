package com.example.socialnetwork

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.Adapter.MessageAdapter
import com.example.socialnetwork.Modal.Chat
import com.example.socialnetwork.Modal.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class MessageActivity : AppCompatActivity() {

    lateinit var profileImage:ImageView
    lateinit var status:TextView
    lateinit var  username:TextView
    lateinit var  recycle:RecyclerView
    lateinit var messageE:EditText
    lateinit var send : ImageButton
    lateinit var db : FirebaseDatabase
    lateinit var  myId : String
    lateinit var id : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        init()
        val intent = intent
        id = intent.getStringExtra("id").toString()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Chat"

        send.setOnClickListener {
            val message = messageE.text.toString()
            if(message.isEmpty())
                return@setOnClickListener
            sendMessage(myId,id,message)

            messageE.setText("")
        }

            db.getReference("myUsers").child(id).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)

                    if(user?.imgUrl.equals("default"))
                        profileImage.setImageResource(R.mipmap.ic_launcher)
                    else{
                        applicationContext?.let { Picasso.Builder(it)
                            .build()
                            .load(user?.imgUrl)
                            .into(profileImage) }

                }
                    status.text = user?.status

                    username.text = user?.username
                    user?.imgUrl?.let { readMessage(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext,"Error " + error.message ,Toast.LENGTH_LONG).show()
                }

            })
    }

    private fun sendMessage(myId: String, id: String, message: String) {
        val chat = Chat(message,myId,id)

        db.getReference("Chats").push().setValue(chat)

    }

    private fun readMessage(imgUrl: String) {
        val chatList: MutableList<Chat> = mutableListOf()
        db.getReference("Chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (snapshot1 in snapshot.children) {
                    val chat = snapshot1.getValue(Chat::class.java)
                    if ((chat?.receiver == myId && chat.sender == id) || (chat?.receiver == id && chat.sender == myId)) {
                        chatList.add(chat)
                    }
                    val adapter = MessageAdapter(this@MessageActivity, chatList, imgUrl)
                    recycle.adapter = adapter
                    recycle.layoutManager = LinearLayoutManager(applicationContext)

                    recycle.scrollToPosition(chatList.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error " + error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    fun checkStatus(status : String){
        val map: HashMap<String, Any> = hashMapOf("status" to status)
        val recefe = db.getReference("myUsers").child(myId)
        recefe.updateChildren(map)
    }

    override fun onPostResume() {
        super.onPostResume()
        checkStatus("online")
    }

    override fun onPause() {
        super.onPause()
        checkStatus("offline")
    }

    fun init(){
        profileImage = findViewById(R.id.chatImage)
        username = findViewById(R.id.chatName)
        status = findViewById(R.id.status)
        recycle = findViewById(R.id.messageR)
        messageE = findViewById(R.id.messageText)
        db = FirebaseDatabase.getInstance()
        send = findViewById(R.id.sendBtn)
        myId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        id = intent.getStringExtra("id").toString()
    }
}