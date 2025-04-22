package com.caramel.mercury.scorer_interface

/**
 * Scorer interface
 *
 * @constructor Create empty Scorer interface
 */
interface ScorerInterface {
    /**
     * Score key
     *
     * @param key
     */
    fun scoreKey(key: String) : Int

    /**
     * Get score
     *
     * @param key
     * @return
     */
    fun getScore(key: String) : Int

    companion object default: ScorerInterface {
        override fun scoreKey(key: String): Int { return 1}

        override fun getScore(key: String): Int {return 0}

    }
}