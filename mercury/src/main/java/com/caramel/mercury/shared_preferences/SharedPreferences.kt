package com.caramel.mercury.shared_preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Shared preferences store
 *
 * @constructor
 *
 * @param context
 * @param prefsName
 */
class SharedPreferencesStore(context: Context, prefsName: String) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    /**
     * Put
     *
     * @param key
     * @param value
     */
    fun put(key: String, value: Any) {
        val editor = prefs.edit()
        when (value) {
            is String -> editor.putString(key, value).apply()
            is Int -> editor.putInt(key, value).apply()
            is Boolean -> editor.putBoolean(key, value).apply()
            is Float -> editor.putFloat(key, value).apply()
            is Long -> editor.putLong(key, value).apply()
            is Set<*> -> {
                if (value.all { it is String }) {
                    @Suppress("UNCHECKED_CAST") editor.putStringSet(key, value as Set<String>)
                } else {
                    throw IllegalArgumentException("Only Set<String> is supported for SharedPreferences.")
                }
            }
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java.name}")
        }
        editor.apply()
    }

    /**
     * Get
     *
     * @param T
     * @param key
     * @param clazz
     * @param defaultValue
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, clazz: Class<T>, defaultValue: T? = null): T? {
        return when (clazz) {
            String::class.java -> prefs.getString(key, defaultValue as? String) as T?
            Int::class.java -> prefs.getInt(key, defaultValue as? Int ?: 0) as T?
            Boolean::class.java -> prefs.getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class.java -> prefs.getFloat(key, defaultValue as? Float ?: 0f) as T?
            Long::class.java -> prefs.getLong(key, defaultValue as? Long ?: 0L) as T?
            Set::class.java, Set::class.javaObjectType -> {
                val defaultSet = defaultValue as? Set<String>
                    ?: throw IllegalArgumentException("Default value must be Set<String>")
                prefs.getStringSet(key, defaultSet) as T?
            }
            else -> throw IllegalArgumentException("Unsupported type: ${clazz.name}")
        }
    }

    /**
     * Remove
     *
     * @param key
     */
    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    /**
     * Clear
     *
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}
