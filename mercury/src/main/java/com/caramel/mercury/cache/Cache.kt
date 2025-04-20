package com.caramel.mercury.cache

/**
 * Cache
 *
 * @param K
 * @param V
 * @constructor Create empty Cache
 */
interface Cache<K : Any, V : Any>{
    /**
     * Put
     *
     * @param key
     * @param value
     */
    fun put(key: K, value: V)

    /**
     * Get
     *
     * @param key
     * @return
     */
    fun get(key: K) : V?

    /**
     * Remove
     *
     * @param key
     */
    fun remove(key: K)

    /**
     * Clear
     *
     */
    fun clear()
}