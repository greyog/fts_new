package com.greyogproducts.greyog.fts.model

import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.greyogproducts.greyog.fts.data.NotificationData
import com.greyogproducts.greyog.fts.data.SummaryItemData
import com.greyogproducts.greyog.fts.data.SummaryListData

class MyModel(private val preferences: SharedPreferences) {
    companion object {
        val defPeriods: MutableSet<String> = mutableSetOf("300", "900", "3600", "18000", "86400", "week", "month")
    }

    //    init {
//        RetrofitHelper.preferences = preferences
//    }
    fun refreshSummaryListData(tabNum: Int, callback: OnSummaryListDataReadyCallback) {

        val defPairs: MutableSet<String> = when (tabNum) {
            2 -> mutableSetOf("169", "166", "14958", "20", "172", "27", "167", "168", "178", "171", "17940")
            1 -> mutableSetOf("7888", "6617", "252", "7997", "6408", "8952", "280", "8193", "6369", "8082", "243", "352", "302", "334", "474", "670", "6974")
            3 -> mutableSetOf("8830", "8836", "8831", "8849", "8833", "8862", "8917")
            else -> mutableSetOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

        }
        val p = setOf<String>().toMutableSet()
        val c = setOf<String>().toMutableSet()

        val prefs = preferences
        val key = "pairs$tabNum"
        prefs.let {
            p += it.getStringSet("periods", defPeriods)
            if (!it.contains(key))
                it.edit().putStringSet(key, defPairs).apply()
            c += it.getStringSet(key, defPairs)
            //, "11", "12", "13", "169", "166", "14958", "20", "172", "27", "167", "168", "178", "171", "17940")
        }

        if (p.isEmpty()) p.addAll(defPeriods)
        if (c.isEmpty()) c.addAll(defPairs)

        getSummaryList(p, c, callback)
    }

    fun getSummaryList(periods: Set<String> = defPeriods, pairs: Set<String>, callback: OnSummaryListDataReadyCallback) {
        RetrofitHelper.getSummaryList(periods, pairs, object : RetrofitHelper.OnResponseListener {
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
