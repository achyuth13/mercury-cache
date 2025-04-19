package com.caramel.mercury.promotion_policy

interface PromotionPolicy {
    fun shouldPromote(currentHeatMapSize : Int, maxHeatMapSize : Int, score : Int, lowestScore : Int) : Boolean
}