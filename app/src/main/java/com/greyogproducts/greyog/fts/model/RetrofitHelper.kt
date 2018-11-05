package com.greyogproducts.greyog.fts.model

import com.greyogproducts.greyog.fts.data.RawSummaryResponseResult
import com.greyogproducts.greyog.fts.data.SearchResponseResult
import com.greyogproducts.greyog.fts.data.SummaryItemData
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.io.IOException

class RetrofitHelper {

    companion object {
        private var mInstance : RetrofitHelper? = null
        private val instance: RetrofitHelper
            get() {
                if (mInstance == null) {
                    mInstance = RetrofitHelper()
                }
                return mInstance!!
            }
//        lateinit var preferences: SharedPreferences

        fun getSummaryList(periods: Set<String>, pairs: Set<String>, listener: OnResponseListener) {
            if (instance.phpSessId == null) {
                instance.doTechRequest(object : OnTechRequestCallBack {
                    override fun onHappySessId() {
                        println("CONNECTION OK, now requesting searchData...")
                        listener.onConnectionOk()
                        instance.doSummaryRequest(periods, pairs, listener)
                    }

                    override fun onBadSessId() {
                        println("NO CONNECTION!")
                        listener.onConnectionError()
                    }

                })
            } else
                instance.doSummaryRequest(periods, pairs, listener)
        }

        fun requestSingleItemData(pairId: String, period: String, listener: OnResponsePairDataCallback) {
            if (instance.phpSessId == null) {
                instance.doTechRequest(object : OnTechRequestCallBack {
                    override fun onHappySessId() {
                        println("CONNECTION OK, now requesting searchData...")
                        listener.onConnectionOk()
                        instance.doSinglePairRequest(pairId, period, listener)
                    }

                    override fun onBadSessId() {
                        println("NO CONNECTION!")
                        listener.onConnectionError()
                    }

                })
            } else
                instance.doSinglePairRequest(pairId, period, listener)
        }

        fun requestSearchData(text: String, listener: OnSearchResponseListener) {
            if (instance.phpSessId == null) {
                instance.doTechRequest(object : OnTechRequestCallBack {
                    override fun onHappySessId() {
                        println("CONNECTION OK, now requesting searchData...")
                        listener.onConnectionOk()
                        instance.doSearchRequest(text, listener)
                    }

                    override fun onBadSessId() {
                        println("NO CONNECTION!")
                        listener.onConnectionError()
                    }

                })
            } else
                instance.doSearchRequest(text, listener)
        }
    }

    private val baseURL = "https://www.investing.com"
    private var phpSessId: String? = null
    private var stickySess: String? = null
    //    private var preferences: SharedPreferences? = null
//    private var onResponseListener: OnResponseListener? = null
//    private var onResponsePairDataListener: OnResponsePairDataCallback? = null
//    private var onSearchResponseListener: OnSearchResponseListener? = null
//    private var prefs: SharedPreferences? = null

    interface OnResponsePairDataCallback : MyResponseCallback {
        fun onDataReady(period: String, raw: String)
    }

    interface OnResponseListener : MyResponseCallback {
        //        fun onResponse(responseResult: SearchResponseResult?)
        fun onSummaryResponse(columns: ArrayList<String>, items: ArrayList<SummaryItemData>)
    }

    interface MyResponseCallback {
        fun onConnectionError()
        fun onConnectionOk()
    }

    interface OnTechRequestCallBack {
        fun onHappySessId()
        fun onBadSessId()
    }

    interface OnSearchResponseListener : MyResponseCallback {
        fun onSearchResponse(response: SearchResponseResult?)
    }

//    init {
//        doTechRequest(listener)
//    }


//    fun setPrefs(prefs: SharedPreferences) {
//        this.preferences = prefs
//        phpSessId = prefs.getString("phpSessID", phpSessId)
//        stickySess = prefs.getString("stickySess", stickySess)
//        doTechRequest()
//    }

    private interface Server {
        @GET("technical/technical-summary")
        fun techRequest(): Call<ResponseBody>

        @Headers("Host: www.investing.com", "Connection: keep-alive", "Content-Length: 44", "Cache-Control: max-age=0", "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 OPR/50.0.2762.67", "Upgrade-Insecure-Requests: 1", "Accept: application/json, text/javascript, */*; q=0.01", "Origin: https://www.investing.com", "X-Requested-With: XMLHttpRequest", "Content-Type: application/x-www-form-urlencoded", "Referer: https://www.investing.com/technical/technical-summary", "Accept-Language: en-GB,en-US;q=0.9,en;q=0.8")//                ,"Accept-Encoding: gzip, deflate, br"
        @FormUrlEncoded
        @POST("search/service/search")
        fun searchRequest(@Field("search_text") searchText: String,
                          @Field("term") term: String,
                          @Field("country_id") countryId: String, // =0
                          @Field("tab_id") tabId: String): Call<SearchResponseResult>  // =All

        @Headers("Host: www.investing.com", "Connection: keep-alive", "Content-Length: 44", "Cache-Control: max-age=0", "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36", "Upgrade-Insecure-Requests: 1", "Accept: application/json, text/javascript, */*; q=0.01", "Origin: https://www.investing.com", "X-Requested-With: XMLHttpRequest", "Content-Type: application/x-www-form-urlencoded", "Referer: https://www.investing.com/technical/technical-summary", "Accept-Language: en-GB,en-US;q=0.9,en;q=0.8")
        @FormUrlEncoded
        @POST("instruments/Service/GetTechincalData")
        fun getSinglePairData(@Field("pairID") pairId: String,
                              @Field("period") period: String,
                              @Field("viewType") viewType: String): Call<ResponseBody>  // = normal

        @Headers("Host: www.investing.com", "Connection: keep-alive", "Accept: application/json, text/javascript, */*; q=0.01", "Origin: https://www.investing.com", "X-Requested-With: XMLHttpRequest", "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36", "Content-Type: application/x-www-form-urlencoded", "Referer: https://www.investing.com/technical/technical-summary", "Accept-Language: en-GB,en-US;q=0.9,en;q=0.8")
        @FormUrlEncoded
        @POST("technical/Service/GetSummaryTable")
        fun getSummaryTable(@Field("tab") tab: String,
                            @Field("options[periods][]") periods: Set<String>,
                            @Field("options[receive_email]") email: String, // = false
                            @Field("options[currencies][]") pairs: Set<String>): Call<RawSummaryResponseResult>
    }

    private class RequestInterceptor(val phpSessId: String?, val stickySess: String?) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val response = chain.proceed(chain.request())
            return response.newBuilder()
                    .addHeader("Cookie", "$phpSessId; $stickySess")
                    .build()
        }
    }

    class ResponseInterceptor(private val listener: OnResponseListener) : Interceptor {

        override fun intercept(chain: Interceptor.Chain?): okhttp3.Response {
            val response = chain!!.proceed(chain.request())
            val body = response.peekBody(Long.MAX_VALUE)
            var raw = body.string()!!
            raw = raw.replace("""\n""","")
            raw = raw.replace("""\t""","")
            raw = raw.replace("""\""","")
//            println(raw)
            val doc = Jsoup.parse(raw)
            val head = doc.getElementsByTag("thead")
            val cols = ArrayList<String>()
            head.select("th").forEach {
                if (it.hasClass("symbol") || it.hasClass("type")) return@forEach
                cols.add(it.text())
            }
            val itemList = ArrayList<SummaryItemData>()
            itemList.add(SummaryItemData())
            val tbody = doc.getElementsByTag("tbody")
            for (tr in tbody.select("tr")) {
                val pid = tr.attr("data-pairid")
                val type = tr.attr("data-row-type")
//                var lastItem = if (itemList.isEmpty()) {
//                    val new = SummaryItemData()
//                    itemList.add(new)
//                    new
//                } else itemList.last()
                var lastItem = itemList.last()
                if (lastItem.pid == null) {
                    lastItem.pid = pid
                }
                if (lastItem.pid != pid) {
                    lastItem = SummaryItemData()
                    itemList.add(lastItem)
                    lastItem.pid = pid
                }
                for (td in tr.select("td")) {
                    if (td.hasClass("symbol")){
                        val a = td.getElementsByTag("a").first()
                        lastItem.name = a.attr("title")
                        lastItem.symbol = a.text()
                        val p = td.getElementsByTag("p").first()
                        lastItem.price = p.text()
                        continue
                    }
                    if (td.hasClass("type")) continue
                    when {
                        type.contains("movingAverages") -> lastItem.mas.add(td.text())
                        type.contains( "indicators") -> lastItem.inds.add(td.text())
                        type.contains( "summary") -> lastItem.sums.add(td.text())
                    }
                }
            }
            listener.onSummaryResponse(cols, itemList)
            return response
        }

    }

    private var triesCount = 0

    fun doTechRequest(listener: OnTechRequestCallBack) {
        triesCount += 1
        val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        val server = retrofit.create(Server::class.java)
        val call = server.techRequest()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
//                    println( "onResponse: " + response.message())
                    if (triesCount <= 10) doTechRequest(listener) else unHappyId()
                } else {
                    triesCount = 0
//                    println("onResponse: ok " + response.headers().values("Set-Cookie"))
                    for (s in response.headers().values("Set-Cookie")) {
                        if (s.toUpperCase().contains("PHPSESSID")) {
                            phpSessId = s.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
//                            println("onResponse: " + phpSessId!!)
//                            preferences!!.edit().putString("phpSessID", phpSessId).apply()
                        }
                        if (s.contains("StickySession")) {
                            stickySess = s.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
//                            println("onResponse: " + stickySess!!)
//                            preferences!!.edit().putString("stickySess", stickySess).apply()
                        }
                    }
                    listener.onHappySessId()
                }
            }

            private fun unHappyId() {
//                println("too many tries")
                listener.onBadSessId()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                if (triesCount <= 10) doTechRequest(listener) else unHappyId()
            }
        })
    }

//    fun doRequest(text: String) {
//        if (phpSessId == null) {
//            println("doRequest: no phpSessID found")
//            return
//        }
//        val client = OkHttpClient.Builder()
//                .addInterceptor(RequestInterceptor(phpSessId, stickySess))
//                .build()
//        val retrofit = Retrofit.Builder()
//                .baseUrl(baseURL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//
//        val server = retrofit.create(Server::class.java)
//        val call = server.searchRequest(text, text, "0", "All")
//        println("doRequest: request = " + call.request().headers().toMultimap())
//
//        call.enqueue(object : Callback<SearchResponseResult> {
//            override fun onResponse(call: Call<SearchResponseResult>, response: Response<SearchResponseResult>) {
//                if (response.isSuccessful) {
//                    //                    println("onResponse: ok, response : " + response.headers().toMultimap().toString());
//                    //                    println("onResponse: body : " + response.body().toString());
//                    onResponseListener?.onResponse(response.body())
//                } else {
//                    println("onResponse: " + response.message())
//                }
//            }
//
//            override fun onFailure(call: Call<SearchResponseResult>, t: Throwable) {
//                t.printStackTrace()
//            }
//        })
//    }

    private fun doSinglePairRequest(pairId: String, period: String, listener: OnResponsePairDataCallback) {

        println("doSinglePairRequest: pair= $pairId, period= $period")
        if (phpSessId == null) {
            println("doRequest: no phpSessID found")
            return
        }
        val client = OkHttpClient.Builder()
                .addInterceptor(RequestInterceptor(phpSessId, stickySess))
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                //                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()

        val server = retrofit.create(Server::class.java)

        val call = server.getSinglePairData(pairId, period, "json")


        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    println("onResponse: " + response.message())
                } else {
//                    println("onDataReady: ok " + response.headers().values("Set-Cookie"))
                    try {
                        listener.onDataReady(period, response.body()!!.string())
                        //                        println("onResponse: ok "+ response.body().string());
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }


    fun doSummaryRequest(periods: Set<String>, pairs: Set<String>, listener: OnResponseListener) {
        if (phpSessId == null) {
            println("doRequest: no phpSessID found")
            return
        }
        val client = OkHttpClient.Builder()
                .addInterceptor(RequestInterceptor(phpSessId, stickySess))
                .addInterceptor(ResponseInterceptor(listener))
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(JspoonConverterFactory.create())
                //                .addConverterFactory(ScalarsConverterFactory.create())
                .build()

        val server = retrofit.create(Server::class.java)

        val call = server.getSummaryTable("forex", periods, "false", pairs)
        call.enqueue(object : Callback<RawSummaryResponseResult> {
            override fun onResponse(call: Call<RawSummaryResponseResult>, responseRaw: Response<RawSummaryResponseResult>) {
                if (responseRaw.isSuccessful) {
//                    all searchData processes at response interceptor
                    //                    println("onResponse: ok, response : " + response.headers().toMultimap().toString());
                    //                    println("onResponse: body : " + response.body().toString());
//                    onResponseListener?.onSummaryResponse(response.body())
                } else {
                    println("onResponse: " + responseRaw.message())
                }
            }

            override fun onFailure(call: Call<RawSummaryResponseResult>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun doSearchRequest(text: String, listener: OnSearchResponseListener) {
        if (phpSessId == null) {
            println("doRequest: no phpSessID found")
            return
        }
        val client = OkHttpClient.Builder()
                .addInterceptor(RequestInterceptor(phpSessId, stickySess))
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val server = retrofit.create(Server::class.java)
        val call = server.searchRequest(text, text, "0", "All")
        call.enqueue(object : Callback<SearchResponseResult> {
            override fun onResponse(call: Call<SearchResponseResult>, response: Response<SearchResponseResult>) {
                if (response.isSuccessful) {
                    listener.onSearchResponse(response.body())
                }else {
                    println("onSearchResponse: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<SearchResponseResult>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

}


