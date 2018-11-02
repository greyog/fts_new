package com.greyogproducts.greyog.fts.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.greyogproducts.greyog.fts.data.NotificationData

class NotificationsListViewModel(app: Application) : AndroidViewModel(app) {
    fun deleteItem(itemId: String) {
        println("tem to delete: $itemId")
        val oldList = notificationList.value ?: emptyList()
        val newList = oldList.filter { it.pairId != itemId }
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(newList)
//        println("newList: $newList")
        println("json after delete: $json")
        prefs.edit().putString("notifications", json).apply()
        refreshList()
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
        println("json from prefs: $json")
        notificationList.value = gson.fromJson(json, object : TypeToken<List<NotificationData>>() {}.type)
    }

    fun addItem(item: NotificationData) {
//        println("item to add: $item")
        val oldList = notificationList.value ?: emptyList()
        val newList = oldList.asSequence().dropWhile { it.pairId == item.pairId }.toMutableList()
        newList.add(item)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(newList)
//        println("newList: $newList")
//        println("json after add: $json")
        prefs.edit().putString("notifications", json).apply()
        refreshList()
    }

}