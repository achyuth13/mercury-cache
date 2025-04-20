package com.caramel.mercury.promotion_policy

/**
 * Top n promotion policy
 *
 * @constructor Create empty Top n promotion policy
 */
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