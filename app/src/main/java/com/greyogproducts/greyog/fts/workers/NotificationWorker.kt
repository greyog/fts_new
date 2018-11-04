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
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.greyogproducts.greyog.fts.MainActivity
import com.greyogproducts.greyog.fts.R
import com.greyogproducts.greyog.fts.model.MyModel


class NotificationWorker(val context: Context, val workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object {
        const val TAG = "notifyService"
        const val myChannelId = "fts_notification_channel"
    }

    override fun doWork(): Result {
        val model = MyModel(PreferenceManager.getDefaultSharedPreferences(context))
        val nl = model.getNotificationList()
        println("${javaClass.simpleName}, notify list size ${nl.size}")
//        Toast.makeText(context,"${javaClass.simpleName}, notify list size ${nl.size}",Toast.LENGTH_SHORT).show()
        triggerNotification()
        return Result.SUCCESS
    }

    private fun triggerNotification() {
        createNotificationChannel()
        val mId = 23
        val mBuilder = NotificationCompat.Builder(context, myChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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