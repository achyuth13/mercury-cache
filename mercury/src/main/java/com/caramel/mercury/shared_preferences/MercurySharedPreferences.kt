package com.caramel.mercury.shared_preferences

import android.content.Context
import com.caramel.mercury.cache.Cache
import com.caramel.mercury.heatmap.HeatMapEntry
import com.caramel.mercury.scorer_interface.ScorerInterface
import com.caramel.mercury.heatmap.HeatMapManager
import com.caramel.mercury.promotion_policy.BottomNEvictionPolicy
import com.caramel.mercury.promotion_policy.EvictionPolicy
import com.caramel.mercury.promotion_policy.PromotionPolicy
import com.caramel.mercury.promotion_policy.TopNPromotionPolicy
import com.caramel.mercury.utils.Logger
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Mercury shared preferences
 *
 * @property scorer
 * @property heatMapSize
 * @property promotionPolicy
 * @constructor
 *
 * @param context
 * @param name
 */
class MercurySharedPreferences(
    context: Context,
    name: String,
    private val scorer: ScorerInterface,
    private val heatMapSize: Int,
    private val promotionPolicy: PromotionPolicy = TopNPromotionPolicy(),
    private val evictionPolicy: EvictionPolicy = BottomNEvictionPolicy()
) : Cache<String, Any> {

    private val TAG = "BENCHMARK"
    private var cleanupJob: Job? = null

    private val gson = Gson()
    private val sharedPreferences = SharedPreferencesStore(context, name)
    private val heatMap = HeatMapManager(context, name, heatMapSize)
    private val localStore: ConcurrentHashMap<String, HeatMapEntry> = ConcurrentHashMap()

    @OptIn(DelicateCoroutinesApi::class)
    override fun put(key: String, value: Any) {
        val json = gson.toJson(value)
        sharedPreferences.put(key, json)
        scorer.scoreKey(key)
        val score = scorer.getScore(key)
        heatMap.put(key, value, score, evictionPolicy)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun get(key: String): Any? {
        val entry = heatMap.get(key)
        if (entry != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val score = scorer.scoreKey(key)
                heatMap.updateScore(key, score)
            }
            return entry.value
        }

        val jsonValue = sharedPreferences.get(key, String::class.java) ?: return null
        val value = gson.fromJson(jsonValue, Any::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            Logger.log(TAG, "Scoring in parallel for $key in prefs ${System.currentTimeMillis()}")
            scorer.scoreKey(key)
            val score = scorer.getScore(key)
            heatMap.updateScore(key, score)
            heatMap.evictIfNeeded(key, value, score, evictionPolicy)
        }

        return value
    }

    private fun startCleanupJob() {
        if (cleanupJob?.isActive == true) return

        cleanupJob = CoroutineScope(Dispatchers.IO).launch {
            updateHeatMap()

            while (isActive) {
                delay(1000L)
                updateHeatMap()
            }
        }
    }

    private fun updateHeatMap() {
        Logger.log("Calling here")
    }

    override fun remove(key: String) {
        heatMap.remove(key)
        sharedPreferences.remove(key)
    }

    override fun clear() {
        heatMap.clear()
        sharedPreferences.clear()
    }
}
