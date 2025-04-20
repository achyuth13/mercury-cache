package com.caramel.mercury.shared_preferences

import android.content.Context
import com.caramel.mercury.Cache
import com.caramel.mercury.scorer_interface.ScorerInterface
import com.caramel.mercury.heatmap.HeatMapManager
import com.caramel.mercury.promotion_policy.PromotionPolicy
import com.caramel.mercury.promotion_policy.TopNPromotionPolicy
import com.caramel.mercury.utils.Logger
import com.google.gson.Gson

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

    private val gson = Gson()
    private val sharedPreferences = SharedPreferencesStore(context, name)
    private val heatMap = HeatMapManager(context, name)

    override fun put(key: String, value: Any) {
        sharedPreferences.put(key, gson.toJson(value))
        scorer.scoreKey(key)
        tryPromote(key, value)
    }

    override fun get(key: String): Any? {
        heatMap.get(key)?.let { (jsonValue, type, _) ->
            scorer.scoreKey(key)
            val newScore = scorer.getScore(key)
            val clazz = try {
                Class.forName(type)
            } catch (e: Exception) {
                return null
            }
            val value = gson.fromJson(jsonValue, clazz)
            heatMap.put(key, value, newScore)
            return value
        }

        val jsonValue = sharedPreferences.get(key, String::class.java) ?: return null

        val value = gson.fromJson(jsonValue, Any::class.java)
        scorer.scoreKey(key)
        tryPromote(key, value)
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

    private fun tryPromote(key: String, value: Any) {
        val score = scorer.getScore(key)

        val alreadyInHeatMap = heatMap.get(key) != null
        val shouldPromote = alreadyInHeatMap || promotionPolicy.shouldPromote(
            heatMap.getAll().size,
            heatMapSize,
            score,
            heatMap.getLowestScore()
        )

        if (shouldPromote) {
            if (heatMap.getAll().size >= heatMapSize) {
                val lowest = heatMap.getLowestScoreKey()
                if (lowest != null) {
                    heatMap.remove(lowest)
                }
            }
            heatMap.put(key, value, score)
        }
        Logger.log("HACK", "🔥 Heatmap AFTER Promotion: ${heatMap.getAll()}")
    }
}
