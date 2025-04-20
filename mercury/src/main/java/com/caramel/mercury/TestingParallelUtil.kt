package com.caramel.mercury

import android.content.Context
import com.caramel.mercury.cache.MercuryCacheType
import com.caramel.mercury.scorer_interface.TestingScorerInterface
import com.caramel.mercury.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TestingParallelUtil {

    fun runParallelTests(context: Context) {
        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            Logger.log("TESTING", "🌟 Starting Test A")
            val mercuryCacheA = MercuryCache.Builder<String, Any>()
                .cacheName("anc")
                .cacheType(MercuryCacheType.MERCURY_SHARED_PREFS)
                .heatMapSize(3)
                .scorerInterface(TestingScorerInterface())
                .build(context)

            mercuryCacheA.put("k11", "v1")
            repeat(2) { mercuryCacheA.get("k1") }

            mercuryCacheA.put("k12", "v2")
            repeat(1) { mercuryCacheA.get("k2") }

            mercuryCacheA.put("k13", "v3")
            repeat(3) { mercuryCacheA.get("k3") }

            mercuryCacheA.put("k14", "v4")
            repeat(5) { mercuryCacheA.get("k4") }

            mercuryCacheA.put("k14", "v4-1")
            mercuryCacheA.put("k16", true)
            repeat(10) { mercuryCacheA.get("k6") }

            mercuryCacheA.put("k15", "v5")
        }

        scope.launch {
            Logger.log("TESTING", "🌟 Starting Test B")
            val mercuryCacheB = MercuryCache.Builder<String, String>()
                .cacheName("testing")
                .cacheType(MercuryCacheType.MERCURY_SHARED_PREFS)
                .scorerInterface(TestingScorerInterface())
                .heatMapSize(5)
                .build(context)

            mercuryCacheB.put("k1", "v1")
            repeat(2) { mercuryCacheB.get("k1") }

            mercuryCacheB.put("k2", "v2")
            repeat(1) { mercuryCacheB.get("k2") }

            mercuryCacheB.put("k3", "v3")
            repeat(3) { mercuryCacheB.get("k3") }

            mercuryCacheB.put("k4", "v4")
            repeat(5) { mercuryCacheB.get("k4") }

            mercuryCacheB.put("k4", "v4-1")
            mercuryCacheB.put("k6", "true")
            repeat(10) { mercuryCacheB.get("k6") }

            mercuryCacheB.put("k5", "v5")
//            Logger.log("TEST_B", "✅ Finished Test B: ${mercuryCacheB.getAll()}")
        }
    }

}