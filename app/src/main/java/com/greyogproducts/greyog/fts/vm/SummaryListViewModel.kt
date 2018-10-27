package com.greyogproducts.greyog.fts.vm

import android.arch.lifecycle.*
import com.greyogproducts.greyog.fts.data.SinglePairData
import com.greyogproducts.greyog.fts.data.SummaryListData
import com.greyogproducts.greyog.fts.model.MyModel
import com.greyogproducts.greyog.fts.model.OnSummaryListDataReadyCallback
import com.greyogproducts.greyog.fts.model.RetrofitHelper

enum class ConnectionState {
    OK, ERROR, LOADING, LOADED
}

class SummaryListViewModel : ViewModel(), LifecycleObserver {
    private val model = MyModel()
    val dataMap = emptyMap<Int, MutableLiveData<SummaryListData>>().toMutableMap()

    val isLoading = MutableLiveData<ConnectionState>()

    fun refreshSummaryList(tabNum: Int) {
        val data: MutableLiveData<SummaryListData> = if (dataMap.contains(tabNum)) dataMap[tabNum]!! else {
            dataMap[tabNum] = MutableLiveData()
            dataMap[tabNum]!!
        }
        isLoading.value = ConnectionState.LOADING
        model.refreshSummaryListData(tabNum, object : OnSummaryListDataReadyCallback {
            override fun onConnectionError() {
                isLoading.postValue(ConnectionState.ERROR)
            }

            override fun onConnectionOk() {
                isLoading.postValue(ConnectionState.OK)
            }

            override fun onDataReady(out: SummaryListData) {
                println("SViewModel.onDataReady for tab $tabNum")
                isLoading.postValue(ConnectionState.LOADED)
                data.postValue(out)
            }
        })
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        println("lifecycle resume")
    }
}

class SummSingleViewModel : ViewModel(), LifecycleObserver {
    private val model = MyModel()
    private val periods = listOf("300", "900", "3600", "18000", "86400", "week", "month")
    val dataMap = emptyMap<Int, MutableLiveData<SinglePairData>>().toMutableMap()
    val isLoading = MutableLiveData<ConnectionState>()
    var pairID = ""

    fun refreshData(tabNum: Int) {
        if (pairID == "") return
        val data: MutableLiveData<SinglePairData> = if (dataMap.contains(tabNum)) dataMap[tabNum]!! else {
            dataMap[tabNum] = MutableLiveData()
            dataMap[tabNum]!!
        }
        isLoading.value = ConnectionState.LOADING
        model.refreshSingleItemData(pairID, periods[tabNum], object : RetrofitHelper.OnResponsePairDataCallback {
            override fun onDataReady(period: String, raw: String) {
                val spd = SinglePairData(period, raw)
                data.postValue(spd)
                isLoading.postValue(ConnectionState.LOADED)
            }

            override fun onConnectionError() {
                isLoading.postValue(ConnectionState.ERROR)
            }

            override fun onConnectionOk() {
                isLoading.postValue(ConnectionState.OK)
            }


        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        println("lifecycle resume")
    }
}

class SearchViewModel : ViewModel(), LifecycleObserver {
    private val model = MyModel()
    val data = MutableLiveData<String>()

    val isLoading = MutableLiveData<ConnectionState>()

    fun searchFor(text: String) {
        isLoading.value = ConnectionState.LOADING
        model.refreshSummaryListData(tabNum, object : OnSummaryListDataReadyCallback {
            override fun onConnectionError() {
                isLoading.postValue(ConnectionState.ERROR)
            }

            override fun onConnectionOk() {
                isLoading.postValue(ConnectionState.OK)
            }

            override fun onDataReady(out: SummaryListData) {
                println("SViewModel.onDataReady for tab $tabNum")
                isLoading.postValue(ConnectionState.LOADED)
                data.postValue(out)
            }
        })
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        println("lifecycle resume")
    }
}