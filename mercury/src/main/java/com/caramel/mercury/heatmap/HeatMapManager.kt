package com.caramel.mercury.heatmap

import android.content.Context
import android.content.SharedPreferences
import com.caramel.mercury.models.HeatMapEntry
import com.caramel.mercury.utils.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HeatMapManager(context: Context, name: String, private val maxSize: Int) {
    private val heatMapPrefs: SharedPreferences =
        context.getSharedPreferences("mercury_heat_map_$name", Context.MODE_PRIVATE)
    private val heatMap: LinkedHashMap<String, HeatMapEntry> = linkedMapOf()
    private val gson = Gson()

    init {
        loadFromPrefs()
    }

    fun get(key: String): HeatMapEntry? = synchronized(this) { heatMap[key] }

    fun put(key: String, value: Any, score: Int) = synchronized(this) {
        if (heatMap.size >= maxSize && !heatMap.containsKey(key)) {
            val coldestKey = heatMap.minByOrNull { it.value.score }?.key
            coldestKey?.let { heatMap.remove(it) }
        }

        val jsonValue = gson.toJson(value)
        val type = value::class.java.name

        heatMap[key] = HeatMapEntry(jsonValue, type, score)
        persist()
    }

    fun remove(key: String) = synchronized(this) {
        heatMap.remove(key)
        persist()
    }

    fun clear() = synchronized(this) {
        heatMap.clear()
        heatMapPrefs.edit().clear().apply()
    }

    fun getAll(): Map<String, HeatMapEntry> = synchronized(this) { heatMap.toMap() }

    fun getLowestScore(): Int = synchronized(this) {
        return heatMap.values.minOfOrNull { it.score } ?: Int.MIN_VALUE
    }

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
