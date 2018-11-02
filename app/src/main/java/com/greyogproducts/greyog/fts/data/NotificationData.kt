package com.greyogproducts.greyog.fts.data

data class NotificationData(
        val pairId: String,
        val symbol: String,
        val description: String,
        val fiveMin: TrendCondition = TrendCondition.ANY,
        val fifteenMin: TrendCondition = TrendCondition.ANY,
        val hour: TrendCondition = TrendCondition.ANY,
        val fiveHour: TrendCondition = TrendCondition.ANY,
        val day: TrendCondition = TrendCondition.ANY,
        val week: TrendCondition = TrendCondition.ANY,
        val month: TrendCondition = TrendCondition.ANY)

enum class TrendCondition {
    ANY, STRONG_BUY, BUY, ANY_BUY, NEUTRAL, STRONG_SELL, SELL, ANY_SELL;
}