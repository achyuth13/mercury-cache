package com.caramel.mercury

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.caramel.mercury.promotion_policy.TopNPromotionPolicy
import com.caramel.mercury.scorer_interface.ScorerInterface
import com.caramel.mercury.shared_preferences.MercurySharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MercurySharedPreferencesTest {

    private lateinit var mercury: MercurySharedPreferences

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scorer = object : ScorerInterface {
            private val scoreMap = mutableMapOf<String, Int>()
            override fun scoreKey(key: String): Int {
                scoreMap[key] = (scoreMap[key] ?: 0) + 1
            }

            override fun getScore(key: String): Int = scoreMap[key] ?: 0
        }

        mercury = MercurySharedPreferences(
            context = context,
            name = "test_prefs",
            scorer = scorer,
            heatMapSize = 3,
            promotionPolicy = TopNPromotionPolicy()
        )
    }

    @Test
    fun testPutAndGet() {
        mercury.put("username", "caramel_user")
        val value = mercury.get("username") as? String
        assertEquals("caramel_user", value)
    }

    @Test
    fun testPromotion() {
        mercury.put("a", "one")
        mercury.put("b", "two")
        mercury.put("c", "three")
        mercury.put("d", "four") // Should evict one if all others promoted

        val promoted = mercury.get("b")
        assertNotNull(promoted)
    }

    @Test
    fun testRemove() {
        mercury.put("tempKey", "temp")
        mercury.remove("tempKey")
        assertNull(mercury.get("tempKey"))
    }

    @Test
    fun testClear() {
        mercury.put("x", "data")
        mercury.put("y", "more")
        mercury.clear()
        assertNull(mercury.get("x"))
        assertNull(mercury.get("y"))
    }
}
