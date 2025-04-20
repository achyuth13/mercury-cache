package com.caramel.mercury.heatmap

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    private val heatMap: LinkedHashMap<String, HeatMapEntry> = linkedMapOf()
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
    fun put(key: String, value: Any, score: Int) = synchronized(this) {
        val jsonValue = gson.toJson(value)
        val type = value::class.java.name
        heatMap[key] = HeatMapEntry(jsonValue, type, score)
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
        val jsonString = gson.toJson(heatMap)
        heatMapPrefs.edit().putString("heatMap", jsonString).apply()
    }

    private fun loadFromPrefs() {
        val jsonString = heatMapPrefs.getString("heatMap", null) ?: return
        val type = object : TypeToken<LinkedHashMap<String, HeatMapEntry>>() {}.type
        val loadedMap: LinkedHashMap<String, HeatMapEntry> = gson.fromJson(jsonString, type)
        heatMap.clear()
        heatMap.putAll(loadedMap)
    }
}
