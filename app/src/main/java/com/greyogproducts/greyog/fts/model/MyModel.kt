package com.greyogproducts.greyog.fts.model

import android.content.SharedPreferences
import com.greyogproducts.greyog.fts.data.SummaryItemData
import com.greyogproducts.greyog.fts.data.SummaryListData

class MyModel(preferences: SharedPreferences) {

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
}

interface OnSummaryListDataReadyCallback : RetrofitHelper.MyResponseCallback {
    fun onDataReady(data: SummaryListData)
}
