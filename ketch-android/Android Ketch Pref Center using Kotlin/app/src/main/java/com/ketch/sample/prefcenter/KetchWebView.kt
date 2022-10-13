package com.ketch.sample.prefcenter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
class KetchWebView(context: Context, attrs: AttributeSet?) : WebView(context, attrs) {

    private lateinit var ketchUrl: String
    var listener: KetchListener? = null

    init {
        isVisible = false
        settings.javaScriptEnabled = true
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
            .build()
        webViewClient = LocalContentWebViewClient(assetLoader)
        WebView.setWebContentsDebuggingEnabled(true)

        addJavascriptInterface(
            PreferenceCenterJavascriptInterface(this),
            "androidListener"
        )

        //receive console messages from the WebView
        webChromeClient = object : WebChromeClient() {
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

    fun init(orgCode: String, property: String, identities: List<Identity> = emptyList()) {
        //pass in the property code and  to be used with the Ketch Smart Tag
        var url =
            "https://appassets.androidplatform.net/assets/index.html?orgCode=$orgCode&propertyName=$property"

        val identitiesJSON = JSONObject()
        identities.forEach { identity ->
            identitiesJSON.put(identity.code, identity.value)
        }

        val encodedIdentities =
            Base64.encodeToString(identitiesJSON.toString().toByteArray(), Base64.DEFAULT)

        url = "$url&encodedIdentities=$encodedIdentities"

        // Uncomment this like to force the CCPA regulations for California
        // url = "$url&swb_region=US-CA&swb_show"

        // Uncomment this like to force the GDPR regulations for Germany
        // url = "$url&swb_region=DE"

        // Uncomment this to force the preferences center to show
        //url = "$url&swb_show=preferences"

        ketchUrl = url

        loadUrl(ketchUrl)
    }

    fun show() {
        loadUrl("about:blank")
        clearHistory()
        loadUrl(ketchUrl)
        isVisible = true
    }

    private class PreferenceCenterJavascriptInterface(private val ketchWebView: KetchWebView) {

        @JavascriptInterface
        fun onInit() {
            Log.d(TAG, "plugin initialized")
        }

        @JavascriptInterface
        fun onCCPAUpdate() {
            onCCPAUpdate(null)
        }

        @JavascriptInterface
        fun onCCPAUpdate(ccpaString: String?) {
            Log.d(TAG, "onCCPAUpdate: $ccpaString")
            runOnMainThread {
                ketchWebView.listener?.onCCPAUpdate(ccpaString)
            }
        }

        @JavascriptInterface
        fun onTCFUpdate() {
            onTCFUpdate(null, null)
        }

        @JavascriptInterface
        fun onTCFUpdate(tcfString: String?, tcfApplies: Int?) {
            Log.d(TAG, "onTCFUpdate: tcfString: $tcfString, tcfApplies: $tcfApplies")
            runOnMainThread {
                ketchWebView.listener?.onTCFUpdate(tcfString, tcfApplies)
            }
        }

        @JavascriptInterface
        fun onNotShow() {
            Log.d(TAG, "onNotShow()")
        }

        @JavascriptInterface
        fun onCancel() {
            Log.d(TAG, "onCancel()")
            runOnMainThread {
                ketchWebView.listener?.onCancel()
                ketchWebView.isVisible = false
            }
        }

        @JavascriptInterface
        fun onSave() {
            Log.d(TAG, "onSave()")
            runOnMainThread {
                ketchWebView.listener?.onSave()
                ketchWebView.isVisible = false
            }
        }

        private fun runOnMainThread(action: ()->Unit) {
            Handler(Looper.getMainLooper()).post {
                action.invoke()
            }
        }
    }

    interface KetchListener {
        fun onCCPAUpdate(ccpaString: String?)
        fun onTCFUpdate(tcfString: String?, tcfApplies: Int?)
        fun onSave()
        fun onCancel()
    }

    companion object {
        val TAG = KetchWebView::class.java.simpleName
    }
}