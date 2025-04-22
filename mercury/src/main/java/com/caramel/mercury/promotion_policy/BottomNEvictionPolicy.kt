package com.caramel.mercury.promotion_policy

class BottomNEvictionPolicy : EvictionPolicy{
    override fun shouldEvict(
        currentHeatMapSize: Int,
        maxHeatMapSize: Int,
        score: Int,
        lowestScore: Int
    ): Boolean {
        return currentHeatMapSize > maxHeatMapSize && score > lowestScore
    }
}