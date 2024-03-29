package com.lymors.lycommons.managers

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit


class MyWorkManager(var appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {

            val data = inputData.getString("key")
            Log.i("TAG", "doWork:$data")
            val notificationManager= MyNotificationManager(appContext)
            notificationManager.createNotification()
//            notificationManager.showNotificationWithReply("sd","sfd" , )

            Result.success()
        } catch (e: Exception) {
            Log.i("TAG", "Error showing notification: ${e.message}")
            Result.retry()
        }
    }

}

    object workerMaker{

        fun doItDaily(
            constraints: Constraints,
            context: Context,
            workerId:String,
            inputData: Data,
        ){
            val periodicRequest = PeriodicWorkRequest.Builder(
                MyWorkManager::class.java, 1, TimeUnit.DAYS)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                .build()
            val instanceWorkManager = WorkManager.getInstance(context)
            instanceWorkManager.enqueue(
                periodicRequest
            )
        }

        fun doWorkPeriodically(
            constraints: Constraints,
            context: Context,
            workerId:String,
            repeatInterval:Long,
            repeatIntervalTimeUnit: TimeUnit,
            inputData: Data
        ){
            val periodicRequest = PeriodicWorkRequest.Builder(
                MyWorkManager::class.java, repeatInterval,repeatIntervalTimeUnit)
                .setConstraints(constraints)
                .setInputData(inputData)
                .setInitialDelay(2, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 2,TimeUnit.MINUTES)
                .build()
            val instanceWorkManager = WorkManager.getInstance(context)
            instanceWorkManager.enqueueUniquePeriodicWork(
                workerId,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }


        fun doWorkExpedited(
            constraints: Constraints,
            context: Context,
            workerId:String,
            inputData: Data
        ){
            val periodicRequest = OneTimeWorkRequestBuilder<MyWorkManager>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            val instanceWorkManager = WorkManager.getInstance(context)
            instanceWorkManager.enqueueUniqueWork(
                workerId,
                ExistingWorkPolicy.KEEP,
                periodicRequest
            )
        }


}



//
//
//

//fun doInFutureWithExactTime(notification:Notification,notificationId:Int){
//    val notificationManager = getSystemService(appContext,NotificationManager::class.java) as NotificationManager
//    notificationManager.notify(notificationId, notification)
//
//
//}
//fun doInFuture(notification:Notification,notificationId:Int){
//    val notificationManager = getSystemService(appContext,NotificationManager::class.java) as NotificationManager
//    notificationManager.notify(notificationId, notification)
//
//}



