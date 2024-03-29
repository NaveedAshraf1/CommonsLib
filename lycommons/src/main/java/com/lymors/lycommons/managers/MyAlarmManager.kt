package com.lymors.lycommons.managers

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.lymors.lycommons.broadcastreceiver.AlarmReceiver
import java.util.Calendar

class MyAlarmManager (private val context:Context){
//    val dateAndTimePicker=DateAndTimeManager() //2
    var calender= Calendar.getInstance()


    var alarmManager=context.getSystemService(AlarmManager::class.java) as AlarmManager
    var intent= Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 13, intent, PendingIntent.FLAG_IMMUTABLE)

    fun createAndPermissionForNotification(context: Activity,activityResultLauncher:ActivityResultLauncher<String>){ // 1
        val myNotificationManager= MyNotificationManager(context)
        myNotificationManager.askNotificationPermission(context,activityResultLauncher)
        myNotificationManager.createNotification()
    }

    fun startAlarmViaIntent(context: Context, myAlarmManager: MyAlarmManager, calendar: Calendar){
        val intent=Intent(context, MyAlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        myAlarmManager.setAlarm("yourData", calendar)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun setAlarm(data: String,calendar: Calendar) { // 3
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
        Toast.makeText(context, "Alarm Successfully set.", Toast.LENGTH_SHORT).show()
    }

    fun cancelAlarm(){ //4
        alarmManager.cancel(pendingIntent)
        Toast.makeText(context, "Alarm cancel", Toast.LENGTH_SHORT).show()
    }




}