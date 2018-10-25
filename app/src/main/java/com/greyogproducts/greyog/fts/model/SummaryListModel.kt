package com.greyogproducts.greyog.fts.model

import com.greyogproducts.greyog.fts.data.SummaryItemData
import com.greyogproducts.greyog.fts.data.SummaryListData

class SummaryListModel {

    fun refreshData(tabNum: Int, callback: OnSummaryListDataReadyCallback) {

        RetrofitHelper.requestSummaryList(tabNum, object : RetrofitHelper.OnResponseListener {
            override fun onSummaryResponse(columns: ArrayList<String>, items: ArrayList<SummaryItemData>) {
                println("SummaryListModel.onSummaryResponse")
                callback.onDataReady(SummaryListData(columns, items))
            }
        })
    }
}

interface OnSummaryListDataReadyCallback {
    fun onDataReady(data: SummaryListData)
}
