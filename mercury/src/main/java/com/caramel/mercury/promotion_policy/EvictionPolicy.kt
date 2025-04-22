package com.caramel.mercury.promotion_policy

interface EvictionPolicy {
    fun shouldEvict(currentHeatMapSize : Int, maxHeatMapSize : Int, score : Int, lowestScore : Int) : Boolean
}