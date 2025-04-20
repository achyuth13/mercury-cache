package com.caramel.mercury.room_db

import com.caramel.mercury.cache.Cache
import com.caramel.mercury.scorer_interface.ScorerInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MercuryRoomDb<T : Any>(
    private val scorer: ScorerInterface,
    private val prefetchQueries: List<PrefetchableQuery<T>>,
    private val heatMapSize: Int
) : Cache<String, T> {

    private val memoryCache = LinkedHashMap<String, T>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            prefetchQueries.take(heatMapSize).forEach { query ->
                val result = query.execute()
                memoryCache[query.queryKey()] = result
                scorer.getScore(query.queryKey())
            }
        }
    }

    override fun put(key: String, value: T) {
        memoryCache[key] = value
    }

    override fun get(key: String): T? = memoryCache[key]

    override fun remove(key: String) {
        memoryCache.remove(key)
    }

    override fun clear() {
        memoryCache.clear()
    }
}
