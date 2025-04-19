package com.caramel.mercury.promotion_policy

class TopNPromotionPolicy : PromotionPolicy {
    override fun shouldPromote(
        currentHeatMapSize: Int,
        maxHeatMapSize: Int,
        score: Int,
        lowestScore: Int
    ): Boolean {
        return currentHeatMapSize < maxHeatMapSize && score > lowestScore
    }
}