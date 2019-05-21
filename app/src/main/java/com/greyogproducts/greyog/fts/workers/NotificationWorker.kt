package com.greyogproducts.greyog.fts.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.TaskStackBuilder
import androidx.work.*
import com.greyogproducts.greyog.fts.MainActivity
import com.greyogproducts.greyog.fts.R
import com.greyogproducts.greyog.fts.data.NotificationData
import com.greyogproducts.greyog.fts.data.SummaryListData
import com.greyogproducts.greyog.fts.data.TrendCondition
import com.greyogproducts.greyog.fts.model.MyModel
import com.greyogproducts.greyog.fts.model.OnSummaryListDataReadyCallback
import java.util.concurrent.TimeUnit


class NotificationWorker(val context: Context, val workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object {
        private const val TAG = "notifyService"
        const val myChannelId = "fts_notification_channel"
        fun triggerNextWorker() {
//            val wrBuilder = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
//            wrBuilder.setInitialDelay(5, TimeUnit.MINUTES)
            val wrBuilder = PeriodicWorkRequest.Builder(NotificationWorker::class.java, 5, TimeUnit.MINUTES)
            wrBuilder.addTag(NotificationWorker.TAG)
            val wr = wrBuilder.build()
            WorkManager.getInstance().enqueueUniquePeriodicWork(myChannelId, ExistingPeriodicWorkPolicy.REPLACE, wr)
        }

        fun cancelWorkers() {
            WorkManager.getInstance().cancelAllWorkByTag(NotificationWorker.TAG)
        }

        fun isWorking(): Boolean {
            var status = false
            val r = WorkManager.getInstance().getWorkInfosByTag(TAG).get().map {
                println("${this::class.java.simpleName}: ${it.id}, ${it.state}")
                status = status || !it.state.isFinished
            }
            return status
        }
    }

    override fun doWork(): Result {
//        triggerNotification(Timestamp(System.currentTimeMillis()).toString())

        checkConditions()
//        val bg: CoroutineContext = CoroutineContext
        return Result.success()
    }

    private fun checkConditions() {
        val model = MyModel(PreferenceManager.getDefaultSharedPreferences(context))
        val nl = model.getNotificationList()
        val c = nl.map { it.pairId }.toSet()
        model.getSummaryList(MyModel.defPeriods,
                c,
                object : OnSummaryListDataReadyCallback {
                    override fun onDataReady(data: SummaryListData) {
                        nl.forEach { nd ->
                            var result = false
                            val sumData = data.items.find { summaryItemData ->
                                summaryItemData.pid == nd.pairId
                            }?.sums?.mapIndexed { index, s ->
                                data.columns[index] to s
                            }.orEmpty().toMap()
                            println("from Notification Worker $nd : $sumData")
                            result = nd.fiveMin.cmpr(sumData[data.columns[0]])
                            result = result && nd.fifteenMin.cmpr(sumData[data.columns[1]])
                            result = result && nd.hour.cmpr(sumData[data.columns[2]])
                            result = result && nd.fiveHour.cmpr(sumData[data.columns[3]])
                            result = result && nd.day.cmpr(sumData[data.columns[4]])
                            result = result && nd.week.cmpr(sumData[data.columns[5]])
                            result = result && nd.month.cmpr(sumData[data.columns[6]])
                            if (result) triggerNotification(nd)


                        }
                    }

                    override fun onConnectionError() {
//                      error
                    }

                    override fun onConnectionOk() {
//                        ok
                    }
                }
        )

    }

    private fun triggerNotification(nd: NotificationData) {
        createNotificationChannel()
        val mId = nd.pairId.toInt()
        val mBuilder = NotificationCompat.Builder(context, myChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Your condition fo ${nd.symbol} match")
                .setContentText("Tap to see more info")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)

// Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, MainActivity::class.java)
// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity::class.java)
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = NotificationManagerCompat.from(context)
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build())
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
// the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            //build the actual notification channel, giving it a unique ID and name
            val channel = NotificationChannel(myChannelId, myChannelId, importance)

            //we can optionally add a description for the channel
            val description = "A channel which shows notifications from Forex Technical Summary app"
            channel.description = description

            //we can optionally set notification LED colour
            channel.lightColor = Color.MAGENTA

            // Register the channel with the system
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

private fun TrendCondition.cmpr(s: String?): Boolean {
    if (s == null) {
        return false
    }
    return when (this) {
        TrendCondition.ANY -> true
        TrendCondition.STRONG_BUY -> s.toUpperCase() == "STRONG BUY"
        TrendCondition.BUY -> s.toUpperCase() == "BUY"
        TrendCondition.ANY_BUY -> s.contains("BUY", true)
        TrendCondition.NEUTRAL -> s.toUpperCase() == "NEUTRAL"
        TrendCondition.STRONG_SELL -> s.toUpperCase() == "STRONG SELL"
        TrendCondition.SELL -> s.toUpperCase() == "SELL"
        TrendCondition.ANY_SELL -> s.contains("SELL", true)
    }
}
