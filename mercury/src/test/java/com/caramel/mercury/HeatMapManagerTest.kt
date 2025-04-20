package com.caramel.mercury

import androidx.test.core.app.ApplicationProvider
import com.caramel.mercury.heatmap.HeatMapManager
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Ordering.Context
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class HeatMapManagerTest {

    private lateinit var context: Context
    private lateinit var heatMapManager: HeatMapManager

    @Before
    fun setup() {
        heatMapManager = HeatMapManager(ApplicationProvider.getApplicationContext(), "test")
    }

    @Test
    fun testPutAndGet() {
        heatMapManager.put("key1", "hello", 10)

        val entry = heatMapManager.get("key1")
        assertNotNull(entry)
        assertEquals("\"hello\"", entry?.value)
        assertEquals("java.lang.String", entry?.type)
        assertEquals(10, entry?.score)
    }

    @Test
    fun testRemove() {
        heatMapManager.put("key1", 123, 5)
        heatMapManager.remove("key1")
        assertNull(heatMapManager.get("key1"))
    }

    @Test
    fun testClear() {
        heatMapManager.put("key1", "x", 1)
        heatMapManager.put("key2", "y", 2)
        heatMapManager.clear()
        assertEquals(0, heatMapManager.getAll().size)
    }

    @Test
    fun testGetLowestScoreAndKey() {
        heatMapManager.put("k1", "a", 3)
        heatMapManager.put("k2", "b", 1)
        heatMapManager.put("k3", "c", 5)

        assertEquals(1, heatMapManager.getLowestScore())
        assertEquals("k2", heatMapManager.getLowestScoreKey())
    }

    @Test
    fun testPersistAndLoad() {
        heatMapManager.put("persistent", 42, 7)

        val newInstance = HeatMapManager(ApplicationProvider.getApplicationContext(), "test")
        val loaded = newInstance.get("persistent")
        assertNotNull(loaded)
        assertEquals(7, loaded?.score)
    }

}