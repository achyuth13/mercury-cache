package com.caramel.mercury

import android.content.Context
import com.caramel.mercury.cache.Cache
import com.caramel.mercury.cache.MercuryCacheType
import com.caramel.mercury.room_db.MercuryRoomDb
import com.caramel.mercury.room_db.PrefetchableQuery
import com.caramel.mercury.scorer_interface.ScorerInterface
import com.caramel.mercury.shared_preferences.MercurySharedPreferences

/**
 * Mercury cache
 *
 * @param K
 * @param V
 * @property cacheName
 * @property cacheType
 * @property scorerInterface
 * @property heatMapSize
 * @constructor Create empty Mercury cache
 */
class MercuryCache<K : Any, V : Any> private constructor(
    private val cacheName: String,
    private val cacheType: MercuryCacheType,
    private val scorerInterface: ScorerInterface,
    private val heatMapSize: Int
) : Cache<K, V> {

    /**
     * Builder
     *
     * @param K
     * @param V
     * @constructor Create empty Builder
     */
    class Builder<K: Any, V : Any> {
        private var cacheName: String = "mercury"
        private var cacheType: MercuryCacheType = MercuryCacheType.MERCURY_SHARED_PREFS
        private var scorerInterface: ScorerInterface = ScorerInterface.default
        private var heatMapSize: Int = 10
        private var enablePrefetch: Boolean = false
        private var prefetchQueries: List<PrefetchableQuery<*>> = emptyList()

        /**
         * Cache name
         *
         * @param cacheName
         */
        fun cacheName(cacheName: String) = apply { this.cacheName = cacheName }

        /**
         * Cache type
         *
         * @param cacheType
         */
        fun cacheType(cacheType: MercuryCacheType) = apply { this.cacheType = cacheType }

        /**
         * Scorer interface
         *
         * @param scorerInterface
         */
        fun scorerInterface(scorerInterface: ScorerInterface) = apply { this.scorerInterface = scorerInterface }

        /**
         * Heat map size
         *
         * @param heatMapSize
         */
        fun heatMapSize(heatMapSize: Int) = apply { this.heatMapSize = heatMapSize }

        /**
         * Enable prefetch
         *
         * @param enablePrefetch
         */
        fun enablePrefetch(enablePrefetch: Boolean) = apply { this.enablePrefetch = enablePrefetch }

        /**
         * Prefetch queries
         *
         * @param queries
         */
        fun prefetchQueries(queries: List<PrefetchableQuery<*>>) = apply {
            this.prefetchQueries = queries
        }

        /**
         * Build
         *
         * @param context
         * @return
         */
        fun build(context : Context) : Cache<K, V> {
            return when (cacheType) {
                MercuryCacheType.MERCURY_SHARED_PREFS -> MercurySharedPreferences(context.applicationContext, cacheName, scorerInterface, heatMapSize) as Cache<K, V>
                MercuryCacheType.MERCURY_ROOM_DB -> {
                    if (!enablePrefetch) throw IllegalStateException("Prefetch must be enabled for ROOM_DB")
                    MercuryRoomDb(
                        scorer = scorerInterface,
                        prefetchQueries = prefetchQueries as List<PrefetchableQuery<Any>>, // you'll need to provide this via builder
                        heatMapSize = heatMapSize
                    ) as Cache<K, V>
                }
            }
        }
    }

    override fun put(key: K, value: V) {}
    override fun get(key: K): V? = null
    override fun remove(key: K) {}
    override fun clear() {}
}
