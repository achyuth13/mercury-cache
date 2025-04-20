package com.caramel.mercury

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.caramel.mercury.shared_preferences.SharedPreferencesStore
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SharedPreferencesStoreTest {

    private lateinit var sharedPreferencesStore: SharedPreferencesStore

    @Before
    fun setup() {
        // Initialize the SharedPreferencesStore with a mock context and test preferences name
        val context: Context = ApplicationProvider.getApplicationContext()
        sharedPreferencesStore = SharedPreferencesStore(context, "test_preferences")
    }

    @Test
    fun testPutAndGetString() {
        sharedPreferencesStore.put("key_string", "value_string")
        val result: String? = sharedPreferencesStore.get("key_string", String::class.java)
        assertEquals("value_string", result)
    }

    @Test
    fun testPutAndGetInt() {
        sharedPreferencesStore.put("key_int", 42)
        val result: Int? = sharedPreferencesStore.get("key_int", Int::class.java)
        assertEquals(42, result)
    }

    @Test
    fun testPutAndGetBoolean() {
        sharedPreferencesStore.put("key_boolean", true)
        val result: Boolean? = sharedPreferencesStore.get("key_boolean", Boolean::class.java)
        assertEquals(true, result)
    }

    @Test
    fun testPutAndGetFloat() {
        sharedPreferencesStore.put("key_float", 3.14f)
        val result: Float? = sharedPreferencesStore.get("key_float", Float::class.java)
        if (result != null) {
            assertEquals(3.14f, result, 0.01f)
        }
    }

    @Test
    fun testPutAndGetLong() {
        sharedPreferencesStore.put("key_long", 123456789L)
        val result: Long? = sharedPreferencesStore.get("key_long", Long::class.java)
        assertEquals(123456789L, result)
    }

    @Test
    fun testPutAndGetSet() {
        val testSet = setOf("item1", "item2", "item3")
        sharedPreferencesStore.put("key_set", testSet)
        val result: Set<String> = sharedPreferencesStore.get("key_set", Set::class.java, setOf<String>()) as Set<String>
        assertEquals(testSet, result)
    }

    @Test
    fun testRemove() {
        sharedPreferencesStore.put("key_remove", "value_remove")
        sharedPreferencesStore.remove("key_remove")
        val result: String? = sharedPreferencesStore.get("key_remove", String::class.java)
        assertNull(result)
    }

    @Test
    fun testClear() {
        sharedPreferencesStore.put("key1", "value1")
        sharedPreferencesStore.put("key2", "value2")
        sharedPreferencesStore.clear()

        val result1: String? = sharedPreferencesStore.get("key1", String::class.java)
        val result2: String? = sharedPreferencesStore.get("key2", String::class.java)

        assertNull(result1)
        assertNull(result2)
    }
}
