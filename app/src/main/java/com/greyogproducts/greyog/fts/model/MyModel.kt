package com.greyogproducts.greyog.fts.model

import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.greyogproducts.greyog.fts.data.NotificationData
import com.greyogproducts.greyog.fts.data.SummaryItemData
import com.greyogproducts.greyog.fts.data.SummaryListData

class MyModel(private val preferences: SharedPreferences) {

    init {
        RetrofitHelper.preferences = preferences
    }
    fun refreshSummaryListData(tabNum: Int, callback: OnSummaryListDataReadyCallback) {

        RetrofitHelper.requestSummaryList(tabNum, object : RetrofitHelper.OnResponseListener {
            override fun onConnectionOk() = callback.onConnectionOk()

            override fun onConnectionError() = callback.onConnectionError()

            override fun onSummaryResponse(columns: ArrayList<String>, items: ArrayList<SummaryItemData>) {
//                println("MyModel.onSummaryResponse, data: $items")
                callback.onDataReady(SummaryListData(columns, items))
            }
        })
    }

    fun refreshSingleItemData(pairId: String, period: String, listener: RetrofitHelper.OnResponsePairDataCallback) {
        RetrofitHelper.requestSingleItemData(pairId, period, listener)
    }

    fun requestSearchData(text: String, listener: RetrofitHelper.OnSearchResponseListener) {
        RetrofitHelper.requestSearchData(text, listener)
    }

    fun getNotificationList(): List<NotificationData> {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = preferences.getString("notifications", "")
        return gson.fromJson(json, object : TypeToken<List<NotificationData>>() {}.type)
    }

    fun setNotificationList(newList: List<NotificationData>) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(newList)
//        println("newList: $newList")
//        println("json after add: $json")
        preferences.edit().putString("notifications", json).apply()
    }
}

interface OnSummaryListDataReadyCallback : RetrofitHelper.MyResponseCallback {
    fun onDataReady(data: SummaryListData)
}
