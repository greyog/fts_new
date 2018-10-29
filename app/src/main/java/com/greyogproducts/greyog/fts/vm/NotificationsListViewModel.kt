package com.greyogproducts.greyog.fts.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.greyogproducts.greyog.fts.data.NotificationData

class NotificationsListViewModel(private val app: Application) : AndroidViewModel(app) {
    fun deleteItem(itemId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

//    private var mList: MutableLiveData<List<NotificationData>>? = null

    val notificationList = MutableLiveData<List<NotificationData>>()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(app)

    init {
        refreshList()
    }

    private fun refreshList() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = prefs.getString("notifications", "")
        notificationList.value = gson.fromJson(json, object : TypeToken<List<NotificationData>>() {}.type)
    }

    fun addItem(item: NotificationData) {
        val oldList = notificationList.value ?: emptyList()
        val newList = oldList.asSequence().dropWhile { it.pairId == item.pairId }.toMutableList()
        newList.add(item)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(newList)
        println("newList: $newList")
        println("json : $json")
        prefs.edit().putString("notifications", json).apply()
        refreshList()
    }

}