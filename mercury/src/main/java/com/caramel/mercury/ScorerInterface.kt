package com.caramel.mercury

interface ScorerInterface {
    fun scoreKey(key: String) : Int
    fun getScore(key: String) : Int

    companion object default: ScorerInterface {
        override fun scoreKey(key: String): Int {return 0}

        override fun getScore(key: String): Int {return 0}

    }
}