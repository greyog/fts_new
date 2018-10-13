package com.greyogproducts.greyog.fts

import android.content.SharedPreferences
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
        val instance : RetrofitHelper
            get() {
                if (mInstance == null) {
                    mInstance = RetrofitHelper()
                }
                return mInstance!!
            }
    }

    private val baseURL = "https://www.investing.com"
    internal var phpSessId: String? = null
    internal var stickySess: String? = null
    //    private var preferences: SharedPreferences? = null
    var onResponseListener: OnResponseListener? = null
    var onResponsePairDataListener: OnResponsePairDataListener? = null
    var onSearchResponseListener: OnSearchResponseListener? = null
    lateinit var prefs: SharedPreferences

    interface OnResponsePairDataListener {
        fun onResponseTechData(period: String, raw: String)
    }

    interface OnResponseListener {
        fun onResponse(responseResult: MyResponseResult?)
        fun onHappySessId()
        fun onBadSessId()
        fun onSummaryResponse(columns: ArrayList<String>, items: ArrayList<SummaryListItem>)
    }

    interface OnSearchResponseListener {
        fun onSearchResponse(response: MyResponseResult?)
    }

    init {
        doTechRequest()
    }


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
                          @Field("tab_id") tabId: String): Call<MyResponseResult>  // =All

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
                            @Field("options[periods][]") periods: MutableSet<String>,
                            @Field("options[receive_email]") email: String, // = false
                            @Field("options[currencies][]") pairs: MutableSet<String>): Call<MyResponseSummaryResult>
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

    class ResponseInterceptor : Interceptor {

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
            val itemList = ArrayList<SummaryListItem>()
            itemList.add(SummaryListItem())
            val tbody = doc.getElementsByTag("tbody")
            for (tr in tbody.select("tr")) {
                val pid = tr.attr("data-pairid")
                val type = tr.attr("data-row-type")
//                var lastItem = if (itemList.isEmpty()) {
//                    val new = SummaryListItem()
//                    itemList.add(new)
//                    new
//                } else itemList.last()
                var lastItem = itemList.last()
                if (lastItem.pid == null) {
                    lastItem.pid = pid
                }
                if (lastItem.pid != pid) {
                    lastItem = SummaryListItem()
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
            instance.onResponseListener?.onSummaryResponse(cols, itemList)
//            println(itemList)
            return response
        }

    }

    private var triesCount = 0

    private fun doTechRequest() {
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
                    println( "onResponse: " + response.message())
                    if (triesCount <= 10) doTechRequest() else unHappyId()
                } else {
                    triesCount = 0
                    println("onResponse: ok " + response.headers().values("Set-Cookie"))
                    for (s in response.headers().values("Set-Cookie")) {
                        if (s.toUpperCase().contains("PHPSESSID")) {
                            phpSessId = s.split("; ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
                            println("onResponse: " + phpSessId!!)
//                            preferences!!.edit().putString("phpSessID", phpSessId).apply()
                        }
                        if (s.contains("StickySession")) {
                            stickySess = s.split("; ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
                            println("onResponse: " + stickySess!!)
//                            preferences!!.edit().putString("stickySess", stickySess).apply()
                        }
                    }
                    onResponseListener?.onHappySessId()
                }
            }

            private fun unHappyId() {
                println("too many tries")
                onResponseListener?.onBadSessId()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                if (triesCount <= 10) doTechRequest() else unHappyId()
            }
        })
    }

    fun doRequest(text: String) {
        if (phpSessId == null) {
            println("doRequest: no phpSessID found")
            return
        }
        val client = OkHttpClient.Builder()
                .addInterceptor(RequestInterceptor(phpSessId,stickySess))
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val server = retrofit.create(Server::class.java)
        val call = server.searchRequest(text, text, "0", "All")
        println("doRequest: request = " + call.request().headers().toMultimap())

        call.enqueue(object : Callback<MyResponseResult> {
            override fun onResponse(call: Call<MyResponseResult>, response: Response<MyResponseResult>) {
                if (response.isSuccessful) {
                    //                    println("onResponse: ok, response : " + response.headers().toMultimap().toString());
                    //                    println("onResponse: body : " + response.body().toString());
                    onResponseListener?.onResponse(response.body())
                } else {
                    println("onResponse: " + response.message())
                }
            }

            override fun onFailure(call: Call<MyResponseResult>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun doSinglePairRequest(pairId: String, period: String) {
        println("doSinglePairRequest: pair= $pairId, period= $period")
        if (phpSessId == null) {
            println("doRequest: no phpSessID found")
            return
        }
        val client = OkHttpClient.Builder()
                .addInterceptor(RequestInterceptor(phpSessId,stickySess))
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
//                    println("onResponseTechData: ok " + response.headers().values("Set-Cookie"))
                    try {
                        onResponsePairDataListener?.onResponseTechData(period, response.body()!!.string())
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


    fun doSummaryRequest() {
        if (phpSessId == null) {
            println("doRequest: no phpSessID found")
            return
        }
        val client = OkHttpClient.Builder()
                .addInterceptor(RequestInterceptor(phpSessId,stickySess))
                .addInterceptor(ResponseInterceptor())
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(JspoonConverterFactory.create())
                //                .addConverterFactory(ScalarsConverterFactory.create())
                .build()

        val server = retrofit.create(Server::class.java)
        val defPeriods = mutableSetOf("300", "900",  "3600" ,"18000","86400", "week", "month")
        val p = prefs.getStringSet("periods", defPeriods )
        val defPairs = mutableSetOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        if (!prefs.contains("pairs"))
            prefs.edit().putStringSet("pairs", defPairs).apply()
        val c = prefs.getStringSet("pairs",defPairs)//, "11", "12", "13", "169", "166", "14958", "20", "172", "27", "167", "168", "178", "171", "17940")
        println("pairs to load: $c")
        if (c.isEmpty()) c.addAll(defPairs)
        val call = server.getSummaryTable("forex", p, "false", c)

        //        call.enqueue(new Callback<ResponseBody>() {
        //            @Override
        //            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
        //                if (!response.isSuccessful()){ println("onResponse: "+response.message());}
        //                else {
        //                    println("onResponseTechData: ok "+ response.headers().values("Set-Cookie"));
        //                    try {
        //                        onResponseListener.onResponseTechData(response.body().string());
        ////                        println("onResponse: ok "+ response.body().string());
        //                    } catch (IOException e) {
        //                        e.printStackTrace();
        //                    }
        //                }
        //            }
        //
        //            @Override
        //            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
        //                t.printStackTrace();
        //            }
        //        });

        //        println("doRequest: request = " + call.request().headers().toMultimap());
        //
        call.enqueue(object : Callback<MyResponseSummaryResult> {
            override fun onResponse(call: Call<MyResponseSummaryResult>, response: Response<MyResponseSummaryResult>) {
                if (response.isSuccessful) {
                    //                    println("onResponse: ok, response : " + response.headers().toMultimap().toString());
                    //                    println("onResponse: body : " + response.body().toString());
//                    onResponseListener?.onSummaryResponse(response.body())
                } else {
                    println("onResponse: " + response.message())
                }
            }

            override fun onFailure(call: Call<MyResponseSummaryResult>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun doSearchRequest(text: String) {
        if (phpSessId == null) {
            println("doRequest: no phpSessID found")
            return
        }
        val client = OkHttpClient.Builder()
                .addInterceptor(RequestInterceptor(phpSessId,stickySess))
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val server = retrofit.create(Server::class.java)
        val call = server.searchRequest(text, text, "0", "All")
        call.enqueue(object : Callback<MyResponseResult> {
            override fun onResponse(call: Call<MyResponseResult>, response: Response<MyResponseResult>) {
                if (response.isSuccessful) {
                    onSearchResponseListener?.onSearchResponse(response.body())
                }else {
                    println("onSearchResponse: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MyResponseResult>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

}


