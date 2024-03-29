package com.lymors.lycommons.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.lymors.lycommons.R

class MyForegroundService(private var destination: Class<*>):Service() {
    var mediaPlayer: MediaPlayer?=null
    companion object {
        private const val CHANNEL_ID = "music_player_channel"
        private const val NOTIFICATION_ID = 11754
    }



    override fun onCreate(){
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.music) // Replace with your music resource ID
        Log.i("TAG", "onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        Log.i("TAG", "onStartCommand()")
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                pauseMusic()
                startMusic()
            } else {
                startMusic()
            }
        }
        return START_STICKY
    }

    private fun startMusic() {
        mediaPlayer?.start()
        showNotification(destination)
    }

    private fun pauseMusic() {
        mediaPlayer?.pause()
    }

    override fun onBind(intent: Intent?): IBinder? {
       return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i("TAG", "createNotificationChannel()")
            val channel = NotificationChannel(CHANNEL_ID, "Music Player Channel", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun showNotification(destination:Class<*>) {
        val notificationIntent = Intent(this, destination::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notify=NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notify)
        Log.i("TAG", "showNotification()()")
    }
}

//    val serviceIntent = Intent(this@SecondActivity, MyForegroundService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(serviceIntent)
//        } else {
//            startService(serviceIntent)
//        }