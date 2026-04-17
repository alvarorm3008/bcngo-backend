package com.example.bcngo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bcngo.R
import com.example.bcngo.model.ShortUser
import com.example.bcngo.network.ApiService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("NOTIS", "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d("NOTIS", "Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            Log.d("NOTIS", "Message Notification Body: ${it.body}")
            checkAndSendNotification(it.title, remoteMessage.data["message"])

            // Enviar broadcast para actualizar el chat
            val intent = Intent("com.example.bcngo.NEW_MESSAGE")
            intent.putExtra("chatId", remoteMessage.data["chat_id"]?.toInt())
            Log.d("NOTIS", "Sending broadcast for chatId: ${remoteMessage.data["chat_id"]}")
            sendBroadcast(intent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("NOTIS", "Refreshed token: $token")
    }

    private fun checkAndSendNotification(title: String?, message: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val context = this@MyFirebaseMessagingService
            val user: ShortUser? = ApiService.getProfile(context)
            Log.d("NOTIS", "User: $user")
            if (user?.notifications == true) {
                Log.d("NOTIS", "Sending notification")
                sendNotification(title, message)
            }
        }
    }

    private fun sendNotification(title: String?, message: String?) {
        val channelId = "default_channel"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Default Channel Description"
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MyFirebaseMessagingService,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
            }
        }
    }
}