package com.lymors.lycommons.managers

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.resources.Compatibility.Api18Impl.setAutoCancel
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.lymors.lycommons.broadcastreceiver.AlarmReceiver
import com.lymors.lycommons.R

class MyNotificationManager(private val context: Context , requestPermissionLauncher: ActivityResultLauncher<String>?=null) {

    init {
        if (requestPermissionLauncher!=null){
            askNotificationPermission(context,requestPermissionLauncher)
        }
    }
  companion object{

      const val CHANNEL_ID1_FOR_ALARM_NOTIFICATION = "CHANNEL_ID1_FOR_ALARM_NOTIFICATION"
      const val CHANNEL_ID2_FOR_ALARM_NOTIFICATION = "CHANNEL_ID2_FOR_ALARM_NOTIFICATION"
      const val alarmNotificationId=1

      const val CHANNEL_ID_FOR_REPLY_NOTIFICATION = "CHANNEL_ID_FOR_REPLY_NOTIFICATION"
      const val REPLY_KEY="REPLY_KEY"
      const val NOTIFICATION_ID_FOR_REPLY_NOTIFICATION = 2

  }

    fun askNotificationPermission(context: Context, requestPermissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }
                context is Activity && context.shouldShowRequestPermissionRationale( android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch( android.Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch( android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }



    fun createNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(CHANNEL_ID1_FOR_ALARM_NOTIFICATION, "Channel 1", NotificationManager.IMPORTANCE_HIGH)
            channel1.description = "this my high channel for notification "

            val manager=getSystemService(context,NotificationManager::class.java)as NotificationManager
            manager.createNotificationChannel(channel1)
        }
    }


    fun showAlarmNotification(context: Context , destination:Class<*>){
        val notification = NotificationCompat.Builder(context, CHANNEL_ID1_FOR_ALARM_NOTIFICATION)
        notification.setContentTitle("Alarm Title.")
        notification.setContentText("Alarm Content.")

        val intent = Intent(context, destination::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val snoozBroadCastIntent = Intent(context, AlarmReceiver::class.java)
        snoozBroadCastIntent.putExtra("alarmAfter", 5)
        val snoozBroadCastPendingIntent = PendingIntent.getBroadcast(context, 1, snoozBroadCastIntent, PendingIntent.FLAG_IMMUTABLE)

        val broadCastCancelIntent = Intent(context, AlarmReceiver::class.java)
        broadCastCancelIntent.putExtra("alarmAfter", -1)
        val broadCastCancelPendingIntent = PendingIntent.getBroadcast(context, -1, broadCastCancelIntent, PendingIntent.FLAG_IMMUTABLE)

        notification.setSmallIcon(R.drawable.alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(Color.GREEN)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .addAction(R.drawable.snooze, "Snooze", snoozBroadCastPendingIntent)
            .addAction(R.drawable.cancel, "Cancel", broadCastCancelPendingIntent)
            .build()

        val manager = getSystemService(context,NotificationManager::class.java) as NotificationManager
        manager.notify(alarmNotificationId, notification.build())
    }



    fun showNotificationWithReply(title:String,message: String , destination: Class<*>) {

        val intent = Intent(context,destination::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var builder:NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                CHANNEL_ID_FOR_REPLY_NOTIFICATION,"channel_name",NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)

            NotificationCompat.Builder(context,notificationChannel.id)
        } else{
            NotificationCompat.Builder(context)
        }

        builder = builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.alarm))
            .setSmallIcon(R.drawable.alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val replyRemote = RemoteInput.Builder(REPLY_KEY).run {
            setLabel("Insert Your Message here")
            build()
        }

        val replyAction = NotificationCompat.Action.Builder(0,"Reply",pendingIntent).addRemoteInput(replyRemote).build()
        builder.addAction(replyAction)
        notificationManager.notify(NOTIFICATION_ID_FOR_REPLY_NOTIFICATION,builder.build())

    }


    fun receiveReplyInput(intent: Intent,context: Context,userReplyInput:(String)->Unit) {
        val replyInput =RemoteInput.getResultsFromIntent(intent)

        if (replyInput!=null){
            val inputReplyString = replyInput.getCharSequence(REPLY_KEY).toString()
            userReplyInput.invoke(inputReplyString)

            val notificationId = NOTIFICATION_ID_FOR_REPLY_NOTIFICATION

            val updateCurrentNotification = NotificationCompat.Builder(context,
                CHANNEL_ID_FOR_REPLY_NOTIFICATION
            )
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.alarm))
                .setSmallIcon(R.drawable.alarm)
                .setContentTitle("Message Sent Success")
                .setContentText("Updated notification")
                .build()
            val notificationManager = getSystemService(context,NotificationManager::class.java) as NotificationManager
            notificationManager.notify(notificationId,updateCurrentNotification)
        }
    }



    fun showNotification(title:String,message: String,channelId:String,channelName: String,notificationId:Int,requestCode:Int,smallIcon:Int=R.drawable.alarm,clazz:Class<*>?=null,bigMessage:String?=null,bigImage:Int?=null,isSticky:Boolean=false){

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var builder:NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
            NotificationCompat.Builder(context,notificationChannel.id)
        } else{
            NotificationCompat.Builder(context)
        }

        builder = builder.apply {
            setSmallIcon(smallIcon)
            setContentTitle(title)
            setContentText(message)
            setDefaults(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
            setOngoing(isSticky)

            if (bigMessage!=null){
                setStyle(NotificationCompat.BigTextStyle().bigText(bigMessage))
            }

            if (bigImage!=null){
                setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(BitmapFactory.decodeResource(context.resources, bigImage))
                    .setBigContentTitle("Click to see message.")
                )
            }

            if (clazz!=null){
                val intent = Intent(context,clazz).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val pendingIntent = PendingIntent.getActivity(context, requestCode,intent,PendingIntent.FLAG_IMMUTABLE)
                setContentIntent(pendingIntent)
            }

        }
        notificationManager.notify(notificationId,builder.build())
    }


}