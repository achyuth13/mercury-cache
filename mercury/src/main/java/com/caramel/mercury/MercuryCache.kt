package com.caramel.mercury

import android.content.Context
import com.caramel.mercury.utils.Logger

enum class MercuryCacheType {
    MERCURY_SHARED_PREFS
}

class MercuryCache<K : Any, V : Any> private constructor(
    val cacheName: String,
    val cacheType: MercuryCacheType,
    val scorerInterface: ScorerInterface,
    val heatMapSize: Int
) : Cache<K, V> {

    class Builder<K: Any, V : Any> {
        private var cacheName: String = "mercury"
        private var cacheType: MercuryCacheType = MercuryCacheType.MERCURY_SHARED_PREFS
        private var scorerInterface: ScorerInterface = ScorerInterface.default
        private var heatMapSize: Int = 10

        fun cacheName(cacheName: String) = apply { this.cacheName = cacheName }
        fun cacheType(cacheType: MercuryCacheType) = apply { this.cacheType = cacheType }
        fun scorerInterface(scorerInterface: ScorerInterface) = apply { this.scorerInterface = scorerInterface }
        fun heatMapSize(heatMapSize: Int) = apply { this.heatMapSize = heatMapSize }

        fun build(context : Context) : Cache<K , V> {
            return when (cacheType) {
                MercuryCacheType.MERCURY_SHARED_PREFS -> MercurySharedPreferences(context.applicationContext, cacheName, scorerInterface, heatMapSize) as Cache<K, V>
            }
        }
    }

    override fun put(key: K, value: V) { Logger.log("HACK", "Coming to parent file") }
    override fun get(key: K): V? = null
    override fun remove(key: K) {}
    override fun clear() {}
}
