package com.ketch.sample.pref

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
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
import com.google.gson.Gson
import com.google.gson.JsonParseException

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
        setWebContentsDebuggingEnabled(true)

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

        identities.forEach { identity ->
            url = "$url&${identity.code}=${identity.value}"
        }

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
        fun hideExperience(status: String?) {
            Log.d(TAG, "hideExperience: $status")
            if (status?.equals(CLOSE, ignoreCase = true) == true
                || status?.equals(SET_CONSENT, ignoreCase = true) == true
            ) {
                runOnMainThread {
                    ketchWebView.listener?.onClose()
                    ketchWebView.isVisible = false
                }
            }
        }

        @JavascriptInterface
        fun usprivacy_updated(ccpaString: String?) {
            Log.d(TAG, "onCCPAUpdate: $ccpaString")
            runOnMainThread {
                ketchWebView.listener?.onCCPAUpdate(ccpaString)
            }
        }

        @JavascriptInterface
        fun tcf_updated(tcfString: String?) {
            Log.d(TAG, "onTCFUpdate: tcfString: $tcfString")
            runOnMainThread {
                ketchWebView.listener?.onTCFUpdate(tcfString)
            }
        }

        @JavascriptInterface
        fun environment(environment: String?) {
            Log.d(TAG, "environment: $environment")
        }

        @JavascriptInterface
        fun regionInfo(regionInfo: String?) {
            Log.d(TAG, "regionInfo: $regionInfo")
        }

        @JavascriptInterface
        fun jurisdiction(jurisdiction: String?) {
            Log.d(TAG, "jurisdiction: $jurisdiction")
        }

        @JavascriptInterface
        fun identities(identities: String?) {
            Log.d(TAG, "identities: $identities")
        }

        @JavascriptInterface
        fun consent(consentJson: String?) {
            // {"purposes":{"essential_services":true,"tcf.purpose_1":true,"analytics":false,"behavioral_advertising":false,"email_marketing":false,"data_broking":false,"somepurpose_key":false},"vendors":[]}
            val consent = try {
                Gson().fromJson(consentJson, Consent::class.java)
            } catch (ex: JsonParseException) {
                Log.e(TAG, ex.message, ex)
            }
            Log.d(TAG, "consent: $consent")
        }

        @JavascriptInterface
        fun willShowExperience(willShowExperience: String?) {
            Log.d(TAG, "willShowExperience: $willShowExperience")
        }

        @JavascriptInterface
        fun showConsentExperience(showConsentExperience: String?) {
            Log.d(TAG, "showConsentExperience: $showConsentExperience")
        }

        @JavascriptInterface
        fun showPreferenceExperience(showPreferenceExperience: String?) {
            Log.d(TAG, "showPreferenceExperience: $showPreferenceExperience")
        }

        private fun runOnMainThread(action: () -> Unit) {
            Handler(Looper.getMainLooper()).post {
                action.invoke()
            }
        }
    }

    interface KetchListener {
        fun onCCPAUpdate(ccpaString: String?)
        fun onTCFUpdate(tcfString: String?)
        fun onClose()
    }

    companion object {
        private val TAG: String = KetchWebView::class.java.simpleName

        private const val WILL_NOT_SHOW = "willNotShow"
        private const val CLOSE = "close"
        private const val SET_CONSENT = "setConsent"
    }
}
