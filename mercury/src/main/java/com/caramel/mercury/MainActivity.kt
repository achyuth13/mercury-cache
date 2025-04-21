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
import com.caramel.mercury.scorer_interface.TestingScorerInterface
import com.caramel.mercury.ui.theme.MercuryapplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createMercuryCache()

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
            .heatMapSize(3)
            .scorerInterface(TestingScorerInterface())
            .build(this)

        // Initial 3 entries
        mercuryCache.put("key1", "value1") // score = 1
        repeat(2) { mercuryCache.get("key1") } // score = 3
//
//        mercuryCache.put("key2", "value2") // score = 1
//        repeat(1) { mercuryCache.get("key2") } // score = 2
//
//        mercuryCache.put("key3", "value3") // score = 1
//        repeat(3) { mercuryCache.get("key3") } // score = 4
//
//        // Insert 4th key -> should trigger eviction of coldest (k2 if scores hold)
//        mercuryCache.put("key4", "value4") // score = 1
//        repeat(5) { mercuryCache.get("key4") } // score = 2
//
//        mercuryCache.put("key4", "value4-1")
//        mercuryCache.put("key6", true)
//        repeat(10) { mercuryCache.get("key6")}
//
//        // Another eviction test
//        mercuryCache.put("key5", "value5") // new insert

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