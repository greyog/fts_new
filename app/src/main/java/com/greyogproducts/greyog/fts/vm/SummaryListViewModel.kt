package com.greyogproducts.greyog.fts.vm

import android.app.Application
import android.arch.lifecycle.*
import android.preference.PreferenceManager
import com.greyogproducts.greyog.fts.data.SearchResponseResult
import com.greyogproducts.greyog.fts.data.SinglePairData
import com.greyogproducts.greyog.fts.data.SummaryListData
import com.greyogproducts.greyog.fts.model.MyModel
import com.greyogproducts.greyog.fts.model.OnSummaryListDataReadyCallback
import com.greyogproducts.greyog.fts.model.RetrofitHelper

enum class ConnectionState {
    OK, ERROR, LOADING, LOADED
}

class SummaryListViewModel(app: Application) : MyViewModel(app) {
    val dataMap = emptyMap<Int, MutableLiveData<SummaryListData>>().toMutableMap()
    var tabNum = 0

    fun refreshSummaryList() {
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
//                println("SViewModel.onDataReady for tab $tabNum")
//                println(out)
                isLoading.postValue(ConnectionState.LOADED)
                isLoading.postValue(null)
                data.postValue(out)
            }
        })
    }

    enum class AddItemResult {
        OK, TOO_MUCH, ERROR, ALREADY_EXISTS
    }

    val addItemResult = MutableLiveData<AddItemResult>()

    fun addItemToList(pairID: String) {
        val prefs = preferences
//        if (prefs == null) {
//            addItemResult.postValue(AddItemResult.ERROR)
//            return
//        }
        val emptSet = emptySet<String>().toMutableSet()
        val key = "pairs$tabNum"
        val idSet = prefs.getStringSet(key, emptSet)

        when {
            idSet.contains(pairID) -> {
                addItemResult.postValue(AddItemResult.ALREADY_EXISTS)
                addItemResult.postValue(null)
            }
            idSet.size >= 20 -> {
                addItemResult.postValue(AddItemResult.TOO_MUCH)
                addItemResult.postValue(null)
            }
            else -> {
                idSet.plusAssign(pairID)
                val newSet = setOf<String>().toMutableSet()
                newSet.addAll(idSet)
                prefs.edit().remove(key).apply()
                prefs.edit().putStringSet(key, newSet).apply()
                println("saved $idSet")
                addItemResult.postValue(AddItemResult.OK)
                addItemResult.postValue(null)
                refreshSummaryList()
            }
        }
    }

    fun deleteFromList(pairID: String) {
        val prefs = preferences
//        if (prefs == null) {
//            addItemResult.postValue(AddItemResult.ERROR)
//            return
//        }
        val emptSet = emptySet<String>().toMutableSet()
        val key = "pairs$tabNum"
        val idSet = prefs.getStringSet(key, emptSet)

        val newSet = setOf<String>().toMutableSet()
        newSet.addAll(idSet.minus(pairID))
        prefs.edit().remove(key).apply()
        prefs.edit().putStringSet(key, newSet).apply()

    }

    fun getSortValue(): Int {
        return preferences.getInt("sort", 1)
    }

    fun setSortValue(i: Int) {
        preferences.edit().putInt("sort", i).apply()

    }
}

class SummSingleViewModel(app: Application) : MyViewModel(app) {
    private val periods = listOf("300", "900", "3600", "18000", "86400", "week", "month")
    val dataMap = emptyMap<Int, MutableLiveData<SinglePairData>>().toMutableMap()
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
//                isLoading.postValue(null)
            }

            override fun onConnectionError() {
                isLoading.postValue(ConnectionState.ERROR)
            }

            override fun onConnectionOk() {
                isLoading.postValue(ConnectionState.OK)
            }
        })
    }
}

class SearchViewModel(app: Application) : MyViewModel(app) {
    val searchData = MutableLiveData<SearchResponseResult>()

    fun searchFor(text: String) {
        isLoading.value = ConnectionState.LOADING
        model.requestSearchData(text, object : RetrofitHelper.OnSearchResponseListener {
            override fun onSearchResponse(response: SearchResponseResult?) {
                if (response == null) {
                    return
                }
                searchData.postValue(response)
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
}

open class MyViewModel(val app: Application) : AndroidViewModel(app), LifecycleObserver {
    val preferences = PreferenceManager.getDefaultSharedPreferences(app)!!
    internal val model: MyModel by lazy {
        MyModel(this.preferences)
    }
    val isLoading = MutableLiveData<ConnectionState>()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        println("lifecycle resume")
    }
}
