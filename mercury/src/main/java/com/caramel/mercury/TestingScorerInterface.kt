package com.caramel.mercury

class TestingScorerInterface : ScorerInterface {

    private val map: MutableMap<String, Int> = mutableMapOf()

    override fun scoreKey(key: String): Int {
        val newScore = (map[key] ?: 0) + 1
        map[key] = newScore
        return newScore
    }

    override fun getScore(key: String): Int {
        return map[key] ?: 1
    }
}
