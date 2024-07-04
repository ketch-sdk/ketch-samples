package com.example.ketchandroidjetpackcomposesampleapp

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import com.example.ketchandroidjetpackcomposesampleapp.ui.theme.KetchAndroidJetpackComposeSampleAppTheme
import com.example.ketchandroidjetpackcomposesampleapp.utils.KetchWebView
import timber.log.Timber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KetchAndroidJetpackComposeSampleAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Greeting("Android")
                    PrivacyChoicesScreen()
                }
            }
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
    KetchAndroidJetpackComposeSampleAppTheme {
        Greeting("Android")
    }
}

@Composable
fun PrivacyChoicesScreen() {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            val ketchWebView = KetchWebView(context, null).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            ketchWebView.isVisible = true
            ketchWebView.settings.javaScriptEnabled = true
            ketchWebView.listener = object : KetchWebView.KetchListener {
                override fun onCCPAUpdate(ccpaString: String?) {
                    Timber.d("LogKetch: ccpaString: $ccpaString")
                }

                override fun onTCFUpdate(tcfString: String?) {
                    TODO("Not yet implemented")
                }

                override fun onClose() {
                    Timber.d("LogKetch: OnClose")
                }
            }
            ketchWebView
        },
        update = {
            val urlText = "<YOUR_URL>"
            it.loadUrl(urlText)
        }
    )
}