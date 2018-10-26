package com.greyogproducts.greyog.fts

import android.arch.lifecycle.*
import com.greyogproducts.greyog.fts.data.SummaryListData
import com.greyogproducts.greyog.fts.model.OnSummaryListDataReadyCallback
import com.greyogproducts.greyog.fts.model.SummaryListModel

class SummaryListViewModel : ViewModel(), LifecycleObserver {
    private val model = SummaryListModel()
    val dataMap = emptyMap<Int, MutableLiveData<SummaryListData>>().toMutableMap()

    val isLoading = MutableLiveData<Boolean>()

    fun refresh(tabNum: Int) {
        val data: MutableLiveData<SummaryListData> = if (dataMap.contains(tabNum)) dataMap[tabNum]!! else {
            dataMap[tabNum] = MutableLiveData()
            dataMap[tabNum]!!
        }
        isLoading.value = true
        model.refreshData(tabNum, object : OnSummaryListDataReadyCallback {
            override fun onDataReady(out: SummaryListData) {
                println("SViewModel.onDataReady for tab $tabNum")
                isLoading.postValue(false)
                data.postValue(out)
            }
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        println("lifecycle resume")
    }
}