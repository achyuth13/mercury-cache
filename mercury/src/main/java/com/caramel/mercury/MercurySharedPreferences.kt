package com.caramel.mercury

import android.content.Context
import com.caramel.mercury.datastores.SharedPreferencesStore
import com.caramel.mercury.heatmap.HeatMapManager
import com.caramel.mercury.promotion_policy.PromotionPolicy
import com.caramel.mercury.promotion_policy.TopNPromotionPolicy
import com.caramel.mercury.utils.Logger
import com.google.gson.Gson

class MercurySharedPreferences(
    context: Context,
    name: String,
    private val scorer: ScorerInterface,
    private val heatMapSize: Int,
    private val promotionPolicy: PromotionPolicy = TopNPromotionPolicy()
) : Cache<String, Any> {

    private val gson = Gson()
    private val store = SharedPreferencesStore(context, name)
    private val heatMap = HeatMapManager(context, name, heatMapSize)

    override fun put(key: String, value: Any) {
        store.put(key, gson.toJson(value))
        scorer.scoreKey(key)
        tryPromote(key, value)
    }

    override fun get(key: String): Any? {
        heatMap.get(key)?.let { (jsonValue, type, _) ->
//            Logger.log("HACK", "🔁 Found in HeatMap: $key → $jsonValue (type: $type)")
            scorer.scoreKey(key)
            val newScore = scorer.getScore(key)
            val clazz = try {
                Class.forName(type)
            } catch (e: Exception) {
                return null
            }
            val obj = gson.fromJson(jsonValue, clazz)
            heatMap.put(key, obj, newScore)
            return obj
        }

        val jsonValue = store.get(key, String::class.java) ?: return null
//        Logger.log("HACK", "🔁 Found in Prefs: $key → $jsonValue")

        val obj = gson.fromJson(jsonValue, Any::class.java)
        scorer.scoreKey(key)
        tryPromote(key, obj)
        return obj
    }

    override fun remove(key: String) {
        heatMap.remove(key)
        store.remove(key)
    }

    override fun clear() {
        heatMap.clear()
        store.clear()
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

//        Logger.log("HACK", "💡 Promotion Decision → promote=$shouldPromote for $key with score=$score")
//        Logger.log("HACK", "📊 HeatMap Size=${heatMap.getAll().size}, heatMapSizeLimit=$heatMapSize, LowestScore=${heatMap.getLowestScore()}")

        if (shouldPromote) {
            if (heatMap.getAll().size >= heatMapSize) {
                val lowest = heatMap.getLowestScoreKey()
                if (lowest != null) {
                    heatMap.remove(lowest)
//                    Logger.log("HACK", "🧹 Evicted from HeatMap → $lowest")
                }
            }
            heatMap.put(key, value, score)
//            Logger.log("HACK", "🔥 PROMOTED to HeatMap → $key: $value")
        }
        Logger.log("HACK", "🔥 Heatmap AFTER Promotion: ${heatMap.getAll()}")
    }


}
