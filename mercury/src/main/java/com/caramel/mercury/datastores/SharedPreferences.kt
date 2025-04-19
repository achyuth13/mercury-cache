package com.caramel.mercury.datastores

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesStore(context: Context, prefsName: String) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun put(key: String, value: Any) {
        when (value) {
            is String -> prefs.edit().putString(key, value).apply()
            is Int -> prefs.edit().putInt(key, value).apply()
            is Boolean -> prefs.edit().putBoolean(key, value).apply()
            is Float -> prefs.edit().putFloat(key, value).apply()
            is Long -> prefs.edit().putLong(key, value).apply()
            is Set<*> -> {
                if (value.all { it is String }) {
                    @Suppress("UNCHECKED_CAST")
                    prefs.edit().putStringSet(key, value as Set<String>)
                } else {
                    throw IllegalArgumentException("Only Set<String> is supported for SharedPreferences.")
                }
            }
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java.name}")
        }
    }

    fun <T> get(key: String, clazz: Class<T>, defaultValue: T? = null): T? {
        return when (clazz) {
            String::class.java -> prefs.getString(key, defaultValue as? String) as T?
            Int::class.java -> prefs.getInt(key, defaultValue as? Int ?: 0) as T?
            Boolean::class.java -> prefs.getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class.java -> prefs.getFloat(key, defaultValue as? Float ?: 0f) as T?
            Long::class.java -> prefs.getLong(key, defaultValue as? Long ?: 0L) as T?
            Set::class.java -> prefs.getStringSet(key, defaultValue as? Set<String>) as T?
            else -> throw IllegalArgumentException("Unsupported type: ${clazz.name}")
        }
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
