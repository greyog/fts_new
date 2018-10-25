package com.greyogproducts.greyog.fts

import android.arch.lifecycle.*
import com.greyogproducts.greyog.fts.data.SummaryListData
import com.greyogproducts.greyog.fts.model.OnSummaryListDataReadyCallback
import com.greyogproducts.greyog.fts.model.SummaryListModel

class SummaryListViewModel : ViewModel(), LifecycleObserver {
    private val model = SummaryListModel()
    val data = MutableLiveData<SummaryListData>()
    val isLoading = MutableLiveData<Boolean>()

    fun refresh(tabNum: Int) {
        isLoading.value = true
        model.refreshData(tabNum, object : OnSummaryListDataReadyCallback {
            override fun onDataReady(out: SummaryListData) {
                println("SViewModel.onDataReady")
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