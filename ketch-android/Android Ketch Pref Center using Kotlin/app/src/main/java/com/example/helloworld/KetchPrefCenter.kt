package com.example.helloworld

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import org.json.JSONObject
import java.io.Serializable

class KetchPrefCenter : AppCompatActivity() {
    var consent:Consent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ketch_pref_center)

        supportActionBar?.hide()

        val identities = intent.getSerializableExtra("identities") as? ArrayList<Identity>
        val myWebView: WebView = findViewById(R.id.webView)
        val webSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(this))
            .build()
        myWebView.webViewClient = LocalContentWebViewClient(assetLoader)
        myWebView.addJavascriptInterface(PreferenceCenterJavascriptInterface(this), "androidListener")
        myWebView.addJavascriptInterface(UserConsentJavascriptInterface(this), "consentListener")

        //pass in the property code and  to be used with the Ketch Smart Tag
        var ketchProperty = intent.getStringExtra("property")
        var ketchOrgCode = intent.getStringExtra("orgCode")
        var url = "https://appassets.androidplatform.net/assets/index.html?orgCode=$ketchOrgCode&propertyName=$ketchProperty"
        var identitiesJSON = JSONObject()
        if (identities != null) {
            for(identity in identities){
                identitiesJSON.put(identity.code, identity.value)
            }
        }

        var encodedIdentities = Base64.encodeToString(identitiesJSON.toString().toByteArray(), Base64.DEFAULT);
        url = "$url&encodedIdentities=$encodedIdentities"

        myWebView.loadUrl(url)

        //receive console messages from the WebView
        myWebView.webChromeClient = object: WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.i("WebViewConsole", consoleMessage.message())
                return true
            }
        }
    }
}

private class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClientCompat() {
    @RequiresApi(21)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }
}

class PreferenceCenterJavascriptInterface(private val context: Context) {
    @JavascriptInterface
    fun receiveMessage(message: String) {
        if (message == "PreferenceCenterClosed") {
            val ketchPrefCenter = (context as? KetchPrefCenter)
            val intent = Intent()
            intent.putExtra("consent", (context as? KetchPrefCenter)?.consent)
            ketchPrefCenter?.setResult(RESULT_OK, intent)
            ketchPrefCenter?.finish()
        }
    }
}

class UserConsentJavascriptInterface(private val context: Context) {
    @JavascriptInterface
    fun receiveMessage(message: String) {
        (context as? KetchPrefCenter)?.consent = Consent(message)
    }
}

class Consent(json: String) : JSONObject(json), Serializable {
    val purposes = Purposes(this.getString("purposes"))
}

class Purposes(json: String) : JSONObject(json), Serializable {
    val analytics: Boolean = this.optBoolean("analytics")
}
