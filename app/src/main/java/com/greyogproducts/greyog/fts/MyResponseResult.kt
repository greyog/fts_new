package com.greyogproducts.greyog.fts

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class MyResponseResult {

    @SerializedName("All")
    @Expose
    @JvmField
    var all: List<All> = ArrayList()

    override fun toString(): String {
        return "All : " + all.toString()
    }

}

class All {
    @SerializedName("pair_ID")
    @Expose
    @JvmField var pairID: Int? = null
    @SerializedName("tab_ID")
    @Expose
    @JvmField
    var tabID: String? = null
    @SerializedName("popularity_rank")
    @Expose
    @JvmField
    var popularityRank: Int? = null
    @SerializedName("link")
    @Expose
    @JvmField
    var link: String? = null
    @SerializedName("symbol")
    @Expose
    @JvmField
    var symbol: String? = null
    @SerializedName("name")
    @Expose
    @JvmField
    var name: String? = null
    @SerializedName("trans_name")
    @Expose
    @JvmField
    var transName: String? = null
    @SerializedName("pair_type")
    @Expose
    @JvmField
    var pairType: String? = null
    @SerializedName("exchange_name_short")
    @Expose
    @JvmField
    var exchangeNameShort: String? = null
    @SerializedName("pair_type_label")
    @Expose
    @JvmField
    var pairTypeLabel: String? = null
    @SerializedName("aql_link")
    @Expose
    @JvmField
    var aqlLink: String? = null
    @SerializedName("aql_pre_link")
    @Expose
    @JvmField
    var aqlPreLink: String? = null
    @SerializedName("country_ID")
    @Expose
    @JvmField
    var countryID: Int? = null
    @SerializedName("flag")
    @Expose
    @JvmField
    var flag: String? = null
    @SerializedName("exchange_popular_symbol")
    @Expose
    @JvmField
    var exchangePopularSymbol: String? = null
    @SerializedName("override_country_ID")
    @Expose
    @JvmField
    var overrideCountryID: Int? = null

    override fun toString(): String {
        return " pairID : " + pairID +
                "; tabID : " + tabID +
                "; popularityRank : " + popularityRank +
                "; link : " + link +
                "; symbol : " + symbol +
                "; name : " + name +
                "; transName : " + transName +
                "; pairType : " + pairType +
                "; exchangeNameShort : " + exchangeNameShort +
                "; pairTypeLabel : " + pairTypeLabel +
                "; aqlLink : " + aqlLink +
                "; aqlPreLink : " + aqlPreLink +
                "; countryID : " + countryID +
                "; flag : " + flag +
                "; exchangePopularSymbol : " + exchangePopularSymbol +
                "; overrideCountryID : " + overrideCountryID
    }
}


class MyResponseSummaryResult {

    @SerializedName("html")
    @Expose
    @JvmField
    var html = ""

//    @SerializedName("html")
//    @Expose
//    @JvmField
//    var r2 = ""

    override fun toString(): String {
        return "html : $html"
    }

}
