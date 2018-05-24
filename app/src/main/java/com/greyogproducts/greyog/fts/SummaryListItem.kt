package com.greyogproducts.greyog.fts

import java.util.ArrayList

class SummaryListItem {
    var pid : String? = null
    var price : String? = null
    val inds = ArrayList<String>()
    val mas = ArrayList<String>()
    val sums = ArrayList<String>()
    var name: String? = null
    override fun toString(): String {
        return "id: $pid, name: $name, sums: $sums\n"
    }
}
