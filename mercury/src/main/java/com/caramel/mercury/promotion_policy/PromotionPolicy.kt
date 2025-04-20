package com.caramel.mercury.promotion_policy

/**
 * Promotion policy
 *
 * @constructor Create empty Promotion policy
 */
interface PromotionPolicy {
    /**
     * Should promote
     *
     * @param currentHeatMapSize
     * @param maxHeatMapSize
     * @param score
     * @param lowestScore
     * @return
     */
    fun shouldPromote(currentHeatMapSize : Int, maxHeatMapSize : Int, score : Int, lowestScore : Int) : Boolean
}