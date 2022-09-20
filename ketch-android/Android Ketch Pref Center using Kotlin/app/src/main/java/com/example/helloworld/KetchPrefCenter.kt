package com.example.helloworld

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.google.gson.Gson
import org.json.JSONObject

class KetchPrefCenter : AppCompatActivity() {
    var consent: Consent? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ketch_pref_center)

        supportActionBar?.hide()

        val identities = intent.getParcelableArrayListExtra<Identity>("identities")
        val myWebView: WebView = findViewById(R.id.webView)
        val webSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(this))
            .build()
        myWebView.webViewClient = LocalContentWebViewClient(assetLoader)
        myWebView.addJavascriptInterface(
            PreferenceCenterJavascriptInterface(this),
            "androidListener"
        )

        //pass in the property code and  to be used with the Ketch Smart Tag
        val ketchProperty = intent.getStringExtra("property")
        val ketchOrgCode = intent.getStringExtra("orgCode")
        var url =
            "https://appassets.androidplatform.net/assets/index.html?orgCode=$ketchOrgCode&propertyName=$ketchProperty"

        val identitiesJSON = JSONObject()
        identities?.forEach { identity ->
            identitiesJSON.put(identity.code, identity.value)
        }

        val encodedIdentities =
            Base64.encodeToString(identitiesJSON.toString().toByteArray(), Base64.DEFAULT)
        url = "$url&encodedIdentities=$encodedIdentities"

        myWebView.loadUrl(url)

        //receive console messages from the WebView
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(TAG, consoleMessage.message())
                return true
            }
        }
    }

    private class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader) :
        WebViewClientCompat() {
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            return assetLoader.shouldInterceptRequest(request.url)
        }
    }

    private class PreferenceCenterJavascriptInterface(private val prefCenter: KetchPrefCenter) {
        @JavascriptInterface
        fun pluginInitialized() {
            Log.d(TAG, "plugin initialized")
        }

        @JavascriptInterface
        fun experienceHidden() {
            val intent = Intent()
            intent.putExtra(CONSENT_KEY, prefCenter.consent)
            prefCenter.setResult(RESULT_OK, intent)
            prefCenter.finish()
        }

        @JavascriptInterface
        fun consentChanged(json: String) {
            prefCenter.consent = Gson().fromJson(json, Consent::class.java)
        }
    }

    companion object {
        private val TAG = KetchPrefCenter::class.java.simpleName

        const val CONSENT_KEY = "consent"
    }
}