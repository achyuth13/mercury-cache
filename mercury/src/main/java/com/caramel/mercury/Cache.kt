package com.caramel.mercury

interface Cache<K : Any, V : Any>{
    fun put(key: K, value: V)
    fun get(key: K) : V?
    fun remove(key: K)
    fun clear()
}