package com.greyogproducts.greyog.fts.data

data class SummaryListData(
        val columns: ArrayList<String>,
        val items: ArrayList<SummaryItemData>
)

data class SinglePairData(
        val period: String,
        val raw: String
)