package com.caramel.mercury.heatmap

import android.content.Context
import android.content.SharedPreferences
import com.caramel.mercury.promotion_policy.EvictionPolicy
import com.caramel.mercury.promotion_policy.PromotionPolicy
import com.caramel.mercury.utils.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Heat map manager
 *
 * @constructor
 *
 * @param context
 * @param name
 */
class HeatMapManager(context: Context, name: String, private val maxHeatMapSize: Int) {
    private val heatMapPrefs: SharedPreferences =
        context.getSharedPreferences("mercury_heat_map_$name", Context.MODE_PRIVATE)
    private val heatMap: ConcurrentHashMap<String, HeatMapEntry> = ConcurrentHashMap()
    private val gson = Gson()
    private val TAG: String = "BENCHMARK"
    private var cleanupJob: Job? = null

    init {
//        loadFromPrefs()
//        startCleanupJob()
    }

    /**
     * Get
     *
     * @param key
     * @return
     */
    fun get(key: String): HeatMapEntry? {
        val heatMap = heatMap[key]
        return heatMap
    }

    /**
     * Put
     *
     * @param key
     * @param value
     * @param score
     */
    fun put(key: String, value: Any, score: Int, evictionPolicy: EvictionPolicy) {
        val type = value.javaClass.name
        heatMap[key] = HeatMapEntry(value, type, score)
        Logger.log(TAG, "heatmap on putting in score is $heatMap")
        evictIfNeeded(key, value, score, evictionPolicy)
//        persist()
    }

    fun updateScore(key: String, score: Int) {
        heatMap[key]?.score = score
        Logger.log(TAG, "Heatmap score on update for $key - ${heatMap[key]?.score}")
    }

    /**
     * Remove
     *
     * @param key
     */
    fun remove(key: String) = synchronized(this) {
        heatMap.remove(key)
//        persist()
    }

    /**
     * Clear
     *
     */
    fun clear() = synchronized(this) {
        heatMap.clear()
        heatMapPrefs.edit().clear().apply()
    }

    /**
     * Get all
     *
     * @return
     */
    fun getAll(): Map<String, HeatMapEntry> = synchronized(this) { heatMap.toMap() }

    /**
     * Get lowest score
     *
     * @return
     */
    fun getLowestScore(): Int = synchronized(this) {
//        Logger.log("BENCHMARK","${heatMap.values}  " )
        return heatMap.values.minOfOrNull { it.score } ?: Int.MIN_VALUE
    }

    /**
     * Get lowest score key
     *
     * @return
     */
    fun getLowestScoreKey(): String? = synchronized(this) {
        return heatMap.entries.minByOrNull { it.value.score }?.key
    }

    fun evictIfNeeded(key: String, value: Any, score: Int, evictionPolicy: EvictionPolicy) =
        synchronized(this) {
            val currentMap = heatMap.toMap()
            val shouldEvict = evictionPolicy.shouldEvict(
                currentMap.size,
                maxHeatMapSize,
                score,
                getLowestScore()
            )

            if (shouldEvict) {
                Logger.log(TAG, "lowest key ${getLowestScoreKey()} score is ${getLowestScore()}")
                getLowestScoreKey()?.let { lowestKey ->
                    heatMap[lowestKey]?.evict = true
                }
                heatMap[key] = HeatMapEntry(value, value.javaClass.name,score)
            }
            Logger.log(TAG, "Heatmap current ${getAll()}")
        }

    private fun startCleanupJob() {
        if (cleanupJob?.isActive == true) return

        cleanupJob = CoroutineScope(Dispatchers.IO).launch {
            // Do an immediate cleanup before the loop
            cleanupEvictedEntries()

            while (isActive) {
                delay(1000L)
                cleanupEvictedEntries()
            }
        }
    }

    private fun cleanupEvictedEntries() {
        val before = heatMap.size
        heatMap.entries.removeIf { it.value.evict }
        val after = heatMap.size
        Logger.log(TAG, "Cleanup ran: Removed ${before - after} evicted entries.")
        Logger.log(TAG, "Heatmap after clean up ${getAll()}")
    }

    fun forceCleanup() {
        CoroutineScope(Dispatchers.IO).launch {
            cleanupEvictedEntries()
        }
    }

    private fun persist() {
        val snapshot = synchronized(this) {
            heatMap.toMap() // create a safe immutable copy
        }

        val json = gson.toJson(snapshot)
        heatMapPrefs.edit().putString("heatMap", json).apply()
    }


    private fun loadFromPrefs() {
        val jsonString = heatMapPrefs.getString("heatMap", null) ?: return
        val type = object : TypeToken<LinkedHashMap<String, HeatMapEntry>>() {}.type
        val loadedMap: LinkedHashMap<String, HeatMapEntry> = gson.fromJson(jsonString, type)
        heatMap.clear()
        heatMap.putAll(loadedMap)
    }
}
