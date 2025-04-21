package com.caramel.mercury.heatmap

import android.content.Context
import android.content.SharedPreferences
import com.caramel.mercury.utils.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Heat map manager
 *
 * @constructor
 *
 * @param context
 * @param name
 */
class HeatMapManager(context: Context, name: String) {
    private val heatMapPrefs: SharedPreferences =
        context.getSharedPreferences("mercury_heat_map_$name", Context.MODE_PRIVATE)
    private val heatMap: ConcurrentHashMap<String, HeatMapEntry> = ConcurrentHashMap()

    private val gson = Gson()

    init {
        loadFromPrefs()
    }

    /**
     * Get
     *
     * @param key
     * @return
     */
    fun get(key: String): HeatMapEntry? = synchronized(this) { heatMap[key] }

    /**
     * Put
     *
     * @param key
     * @param value
     * @param score
     */
    fun put(key: String, value: Any, score: Int) {
        val type = value.javaClass.name
        heatMap[key] = HeatMapEntry(value, type, score)
        persist()
    }


    /**
     * Remove
     *
     * @param key
     */
    fun remove(key: String) = synchronized(this) {
        heatMap.remove(key)
        persist()
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
        Logger.log("BENCHMARK","${heatMap.values}  " )
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
