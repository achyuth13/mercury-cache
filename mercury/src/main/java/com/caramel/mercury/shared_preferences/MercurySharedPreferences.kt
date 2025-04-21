package com.caramel.mercury.shared_preferences

import android.content.Context
import com.caramel.mercury.cache.Cache
import com.caramel.mercury.scorer_interface.ScorerInterface
import com.caramel.mercury.heatmap.HeatMapManager
import com.caramel.mercury.promotion_policy.PromotionPolicy
import com.caramel.mercury.promotion_policy.TopNPromotionPolicy
import com.caramel.mercury.utils.Logger
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
    private val promotionPolicy: PromotionPolicy = TopNPromotionPolicy()
) : Cache<String, Any> {

    private val TAG = "BENCHMARK"

    private val gson = Gson()
    private val sharedPreferences = SharedPreferencesStore(context, name)
    private val heatMap = HeatMapManager(context, name)

    @OptIn(DelicateCoroutinesApi::class)
    override fun put(key: String, value: Any) {
        val json = gson.toJson(value)
        sharedPreferences.put(key, json)
        GlobalScope.launch(Dispatchers.IO) {
            scorer.scoreKey(key)
            val score = scorer.getScore(key)
            tryPromote(key, value, score)
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun get(key: String): Any? {
        val entry = heatMap.get(key)
        Logger.log(TAG, "Entry for heatmap $entry")
        if (entry != null) {
            GlobalScope.launch(Dispatchers.IO) {
                Logger.log(TAG, "Scoring in parallel for $entry in heatmap ${System.currentTimeMillis()}")
                scorer.scoreKey(key)
                val score = scorer.getScore(key)
                tryPromote(key, entry.value, score)
            }
            Logger.log(TAG, "Fetching from heatmap for $key and the value is ${entry.value}")
            return entry.value
        }

        // Slow path: Read from disk
        val jsonValue = sharedPreferences.get(key, String::class.java) ?: return null
        val value = gson.fromJson(jsonValue, Any::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            Logger.log(TAG, "Scoring in parallel for $key in prefs ${System.currentTimeMillis()}")
            scorer.scoreKey(key)
            val score = scorer.getScore(key)
            tryPromote(key, value, score)
        }

        return value
    }

    override fun remove(key: String) {
        heatMap.remove(key)
        sharedPreferences.remove(key)
    }

    override fun clear() {
        heatMap.clear()
        sharedPreferences.clear()
    }

    @Synchronized
    private fun tryPromote(key: String, value: Any, score: Int) {
        val currentMap = heatMap.getAll()
        val shouldPromote = promotionPolicy.shouldPromote(
            currentMap.size,
            heatMapSize,
            score,
            heatMap.getLowestScore()
        )

        Logger.log(TAG, "Checking promotion for $key with score=$score = $shouldPromote")

        if (shouldPromote) {
            Logger.log(TAG, "Promoting $key with $value and Score $score")
            if (currentMap.size >= heatMapSize) {
                Logger.log(TAG, "Lowest score ${heatMap.getLowestScoreKey()}")
                heatMap.getLowestScoreKey()?.let { heatMap.remove(it) }
            }
            heatMap.put(key, value, score)
        }

        Logger.log(TAG, "Heatmap AFTER Promotion: ${heatMap.getAll()}")
    }

}
