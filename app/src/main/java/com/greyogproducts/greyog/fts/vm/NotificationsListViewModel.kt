package com.greyogproducts.greyog.fts.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.preference.PreferenceManager
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.greyogproducts.greyog.fts.data.NotificationData
import com.greyogproducts.greyog.fts.model.MyModel
import com.greyogproducts.greyog.fts.workers.NotificationWorker
import java.util.concurrent.TimeUnit

class NotificationsListViewModel(app: Application) : AndroidViewModel(app) {
    private val model = MyModel(PreferenceManager.getDefaultSharedPreferences(app))

    fun deleteItem(itemId: String) {
        println("tem to delete: $itemId")
        val oldList = notificationList.value ?: emptyList()
        val newList = oldList.filter { it.pairId != itemId }
        model.setNotificationList(newList)
        refreshList()
    }

//    private var mList: MutableLiveData<List<NotificationData>>? = null

    val notificationList = MutableLiveData<List<NotificationData>>()

    init {
        refreshList()
    }

    private fun refreshList() {
        notificationList.value = model.getNotificationList()
    }

    fun addItem(item: NotificationData) {
//        println("item to add: $item")
        val oldList = notificationList.value ?: emptyList()
        val newList = oldList.asSequence().dropWhile { it.pairId == item.pairId }.toMutableList()
        newList.add(item)
        model.setNotificationList(newList)
        refreshList()
    }

    fun turnNotificationServiceOn(isOn: Boolean) {
        if (isOn) {
            val wrBuilder = PeriodicWorkRequest.Builder(NotificationWorker::class.java, 1, TimeUnit.MINUTES)
            wrBuilder.addTag(NotificationWorker.TAG)
            val wr = wrBuilder.build()
            WorkManager.getInstance().enqueue(wr)
        } else {
            WorkManager.getInstance().cancelAllWorkByTag(NotificationWorker.TAG)
        }
    }

}