package com.greyogproducts.greyog.fts3

import java.util.ArrayList

class SummaryListItem {
    var pid : String? = null
    var price : String? = null
    val inds = ArrayList<String>()
    val mas = ArrayList<String>()
    val sums = ArrayList<String>()
    var name: String? = null
    var symbol: String? = null

    override fun toString(): String {
        return "id: $pid, symbol: $symbol, name: $name, sums: $sums\n"
    }
}
