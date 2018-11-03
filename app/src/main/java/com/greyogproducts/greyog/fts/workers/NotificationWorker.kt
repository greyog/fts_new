package com.greyogproducts.greyog.fts.workers

import android.content.Context
import android.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.greyogproducts.greyog.fts.model.MyModel

class NotificationWorker(val context: Context, val workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object {
        const val TAG = "notifyService"
    }

    override fun doWork(): Result {
        val model = MyModel(PreferenceManager.getDefaultSharedPreferences(context))
        val nl = model.getNotificationList()
        println("${javaClass.simpleName}, notify list size ${nl.size}")
//        Toast.makeText(context,"${javaClass.simpleName}, notify list size ${nl.size}",Toast.LENGTH_SHORT).show()
        return Result.SUCCESS
    }
}