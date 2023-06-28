package com.example.socialnetwork

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener{task->
            if(task.isSuccessful)
                Log.d("token",task.result)
        }
    }
}
