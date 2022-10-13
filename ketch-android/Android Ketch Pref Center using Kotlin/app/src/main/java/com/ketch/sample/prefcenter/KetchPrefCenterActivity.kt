package com.ketch.sample.prefcenter

import android.annotation.SuppressLint
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
import com.google.gson.JsonParseException
import org.json.JSONObject

class KetchPrefCenterActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ketch_pref_center)

        supportActionBar?.hide()

        val ketchOrgCode = intent.getStringExtra(ORG_CODE_KEY)
        val ketchProperty = intent.getStringExtra(PROPERTY_KEY)
        val identities = intent.getParcelableArrayListExtra<Identity>(IDENTITIES_KEY)

        val webView: WebView = findViewById(R.id.webView)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(this))
            .build()
        webView.webViewClient = LocalContentWebViewClient(assetLoader)
        WebView.setWebContentsDebuggingEnabled(true)

        webView.addJavascriptInterface(
            PreferenceCenterJavascriptInterface(this),
            "androidListener"
        )

        //pass in the property code and  to be used with the Ketch Smart Tag
        var url =
            "https://appassets.androidplatform.net/assets/index.html?orgCode=$ketchOrgCode&propertyName=$ketchProperty"

        val identitiesJSON = JSONObject()
        identities?.forEach { identity ->
            identitiesJSON.put(identity.code, identity.value)
        }

        val encodedIdentities =
            Base64.encodeToString(identitiesJSON.toString().toByteArray(), Base64.DEFAULT)
        url = "$url&encodedIdentities=$encodedIdentities"

        // Uncomment this like to force the CCPA regulations for California
        // url = "$url&swb_region=US-CA&swb_show"

        // Uncomment this like to force the GDPR regulations for Germany
        // url = "$url&swb_region=DE"

        // Uncomment this to force the preferences senter to show
        url = "$url&swb_show=preferences"

        webView.loadUrl(url)

        //receive console messages from the WebView
        webView.webChromeClient = object : WebChromeClient() {
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

    private class PreferenceCenterJavascriptInterface(private val prefCenterActivity: KetchPrefCenterActivity) {
        private val sharedPreferences = KetchSharedPreferences(prefCenterActivity)

        @JavascriptInterface
        fun onInit() {
            Log.d(TAG, "plugin initialized")
        }

        @JavascriptInterface
        fun onCCPAUpdate(ccpaString: String) {
            // TODO: save the ccpaString to user preferences
            val data = Consent(ccpaString, null, null);
            sharedPreferences.save(data)
        }

        @JavascriptInterface
        fun onTCFUpdate(tcfString: String, tcfApplies: Int) {
            // TODO: save the tcfString to user preferences
            val data = Consent(null, tcfString, tcfApplies);
            sharedPreferences.save(data)
        }

        @JavascriptInterface
        fun onNotShow() {
            prefCenterActivity.setResult(RESULT_FIRST_USER)
            // prefCenterActivity.finish()
        }

        @JavascriptInterface
        fun onClose() {
            prefCenterActivity.setResult(RESULT_CANCELED)
            prefCenterActivity.finish()
        }

        @JavascriptInterface
        fun onSave() {
            prefCenterActivity.setResult(RESULT_OK)
            prefCenterActivity.finish()
        }
    }

    companion object {
        private val TAG = KetchPrefCenterActivity::class.java.simpleName

        const val ORG_CODE_KEY = "orgCode"
        const val PROPERTY_KEY = "property"
        const val IDENTITIES_KEY = "identities"
    }
}
