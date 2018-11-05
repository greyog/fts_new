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

    companion object {
        fun fromString(s: String): TrendCondition {
            return when (s) {
                "Buy" -> TrendCondition.BUY
                "Sell" -> TrendCondition.SELL
                "Any Buy" -> TrendCondition.ANY_BUY
                "Any Sell" -> TrendCondition.ANY_SELL
                "Neutral" -> TrendCondition.NEUTRAL
                "Strong Buy" -> TrendCondition.STRONG_BUY
                "Strong Sell" -> TrendCondition.STRONG_SELL
                else -> TrendCondition.ANY
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            TrendCondition.ANY -> "Any"
            TrendCondition.STRONG_BUY -> "Strong Buy"
            TrendCondition.BUY -> "Buy"
            TrendCondition.ANY_BUY -> "Any Buy"
            TrendCondition.NEUTRAL -> "Neutral"
            TrendCondition.STRONG_SELL -> "Strong Sell"
            TrendCondition.SELL -> "Sell"
            TrendCondition.ANY_SELL -> "Any Sell"
        }
    }
}
