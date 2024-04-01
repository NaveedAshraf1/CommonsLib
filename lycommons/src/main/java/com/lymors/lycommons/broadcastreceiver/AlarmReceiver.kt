package com.lymors.lycommons.broadcastreceiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.getSystemService
import com.lymors.lycommons.managers.MyNotificationManager.Companion.alarmNotificationId
import com.lymors.lycommons.managers.MyAlarmManager
import com.lymors.lycommons.managers.MyNotificationManager
import java.util.Calendar

class AlarmReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context!=null){
            val alarm= MyAlarmManager(context)
            val c = Calendar.getInstance()
            val manager = getSystemService(context, NotificationManager::class.java) as NotificationManager
            val time=intent?.getIntExtra("alarmAfter" , 0)

//            myNotificationManager.showAlarmNotification(context)

            if (time!=0&&time!=-1){
                c[Calendar.HOUR_OF_DAY] = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
                c.add(Calendar.MINUTE,time!!)
                c[Calendar.SECOND]=0
                c[Calendar.MILLISECOND]=0
                alarm.setAlarm("jgsdgjsf", c)
                manager.cancel(alarmNotificationId)
            }else if (time==-1){
                manager.cancel(alarmNotificationId)
            }
        }
    }


}