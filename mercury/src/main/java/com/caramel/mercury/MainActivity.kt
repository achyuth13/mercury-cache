package com.caramel.mercury

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.caramel.mercury.cache.MercuryCacheType
import com.caramel.mercury.heatmap.HeatMapManager
import com.caramel.mercury.scorer_interface.TestingScorerInterface
import com.caramel.mercury.ui.theme.MercuryapplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        createMercuryCache()
        val benchmark = Benchmark()
        benchmark.benchmarkWriteSharedPreferences(this)
        benchmark.benchmarkReadSharedPreferences(this)
        benchmark.benchmarkWriteMercurySharedPreferences(this)
        benchmark.benchmarkReadMercurySharedPreferences(this)
        setContent {
            MercuryapplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun createMercuryCache() {
        val mercuryCache = MercuryCache.Builder<String, Any>()
            .cacheName("anc")
            .cacheType(MercuryCacheType.MERCURY_SHARED_PREFS)
            .heatMapSize(1)
            .scorerInterface(TestingScorerInterface())
            .build(this)

        // Initial 3 entries
        mercuryCache.put("key1", "value1") // score = 1
        repeat(2) { mercuryCache.get("key1") } // score = 3
        mercuryCache.put("key2", "value2") // score = 1
        repeat(3) { mercuryCache.get("key2") } // score = 3
        repeat(6) { mercuryCache.get("key1") }

        CoroutineScope(Dispatchers.Default).launch {
            delay(1000L)
            repeat(2) { mercuryCache.get("key2") } // new access after 1 second
        }

    }

}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MercuryapplicationTheme {
        Greeting("Android")
    }
}