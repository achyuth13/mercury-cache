package com.caramel.mercury

import android.content.Context
import android.util.LruCache

import com.caramel.mercury.cache.MercuryCacheType
import com.caramel.mercury.scorer_interface.TestingScorerInterface
import com.caramel.mercury.utils.Logger
import com.google.gson.Gson
import kotlin.system.measureNanoTime

class Benchmark {

    private val TAG = "BENCHMARKTEST"

    private val gson = Gson()
    private val keys = listOf("key1", "key2", "key3", "key4")
    private val values = listOf("value1", "value2", "value3", "value4")
    val memoryCache = object : LruCache<String, String>(keys.size) {}

    // 🔵 1. Benchmark WRITE — SharedPreferences
    fun benchmarkWriteSharedPreferences(context: Context) {
        val sharedPrefs = context.getSharedPreferences("shared_prefs_benchmark", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        val time = measureNanoTime {
            for (i in keys.indices) {
                editor.putString(keys[i], values[i])
            }
            editor.apply()
        }

        Logger.log(TAG, "SharedPreferences WRITE time: $time ns")
    }

    // 🔵 2. Benchmark READ — SharedPreferences
    fun benchmarkReadSharedPreferences(context: Context) {
        val sharedPrefs = context.getSharedPreferences("shared_prefs_benchmark", Context.MODE_PRIVATE)

        val readTimes = mutableMapOf<String, MutableList<Long>>()

        for (key in keys) {
            val times = mutableListOf<Long>()
            repeat(4) {
                val time = measureNanoTime {
                    val value = sharedPrefs.getString(key, null)
//                    val jsonValue = gson.fromJson(value, String::class.java)
                }
                times.add(time)
            }
            readTimes[key] = times
        }

        Logger.log(TAG, "🗂 SharedPreferences READ times per key (ns): $readTimes")
    }

    // 🔴 1. Benchmark WRITE — LruCache
    fun benchmarkWriteLruCache() {
        val time = measureNanoTime {
            for (i in keys.indices) {
                memoryCache.put(keys[i], values[i])
            }
        }

        Logger.log(TAG, "LruCache WRITE time: $time ns")
    }

    // 🔴 2. Benchmark READ — LruCache
    fun benchmarkReadLruCache() {
        val readTimes = mutableMapOf<String, MutableList<Long>>()

        for (key in keys) {
            val times = mutableListOf<Long>()
            repeat(4) {
                val time = measureNanoTime {
                    val value = memoryCache.get(key)
                    // Optionally simulate deserialization if needed
//                val jsonValue = gson.fromJson(value, String::class.java)
                }
                times.add(time)
            }
            readTimes[key] = times
        }

        Logger.log(TAG, "📁 LruCache READ times per key (ns): $readTimes")
    }



    // 🟣 3. Benchmark WRITE — MercurySharedPreferences
    fun benchmarkWriteMercurySharedPreferences(context: Context) {
        val mercuryCache = MercuryCache.Builder<String, Any>()
            .cacheName("mercury_benchmark")
            .cacheType(MercuryCacheType.MERCURY_SHARED_PREFS)
            .heatMapSize(3)
            .scorerInterface(TestingScorerInterface())
            .build(context)

        val time = measureNanoTime {
            for (i in keys.indices) {
                mercuryCache.put(keys[i], values[i])
            }
        }

        Logger.log(TAG, "Mercury WRITE time: $time ns")
    }

    // 🟣 4. Benchmark READ — MercurySharedPreferences
    fun benchmarkReadMercurySharedPreferences(context: Context) {
        val mercuryCache = MercuryCache.Builder<String, Any>()
            .cacheName("mercury_benchmark")
            .cacheType(MercuryCacheType.MERCURY_SHARED_PREFS)
            .heatMapSize(3)
            .scorerInterface(TestingScorerInterface())
            .build(context)

        // ✅ Pre-fill heatMap to avoid fetching from SharedPreferences
        for (i in keys.indices) {
            mercuryCache.put(keys[i], values[i])
        }

        val readTimes = mutableMapOf<String, MutableList<Long>>()

        for (key in keys) {
            val times = mutableListOf<Long>()
            repeat(4) {
                val time = measureNanoTime {
                    mercuryCache.get(key)
                }
                times.add(time)
            }
            readTimes[key] = times
        }

        Logger.log(TAG, "📦 Mercury READ times per key (ns): $readTimes")
    }


}
