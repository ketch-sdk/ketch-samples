package com.ketch.sample.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import java.util.List;

public class KetchWebView extends WebView {
    private static final String TAG = KetchWebView.class.getSimpleName();

    private static final String WILL_NOT_SHOW = "willNotShow";
    private static final String CLOSE = "close";
    private static final String SET_CONSENT = "setConsent";

    private KetchListener listener;

    private String ketchUrl;

    public KetchWebView(@NonNull Context context) {
        this(context, null);
    }

    public KetchWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KetchWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public KetchWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setVisibility(View.GONE);

        getSettings().setJavaScriptEnabled(true);

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(context))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(context))
                .build();

        setWebViewClient(new LocalContentWebViewClient(assetLoader));
        setWebContentsDebuggingEnabled(true);

        addJavascriptInterface(
                new PreferenceCenterJavascriptInterface(this),
                "androidListener"
        );

        //receive console messages from the WebView
        WebChromeClient webChromeClient = new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, consoleMessage.message());
                return true;
            }
        };
    }

    public void setListener(KetchListener listener) {
        this.listener = listener;
    }

    void init(String orgCode, String property, List<Identity> identities) {
        //pass in the property code and  to be used with the Ketch Smart Tag
        StringBuilder url = new StringBuilder();
        url.append(String.format("https://appassets.androidplatform.net/assets/index.html?orgCode=%1$s&propertyName=%2$s", orgCode, property));

        for (Identity identity : identities) {
            url.append(String.format("&%1$s=%2$s", identity.getCode(), identity.getValue()));
        }

        ketchUrl = url.toString();

        loadUrl(ketchUrl);
    }

    private static class LocalContentWebViewClient extends WebViewClientCompat {
        private final WebViewAssetLoader assetLoader;

        private LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            this.assetLoader = assetLoader;
        }

        @Override
        @Nullable
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return assetLoader.shouldInterceptRequest(request.getUrl());
        }
    }

    public void show() {
        loadUrl("about:blank");
        clearHistory();
        loadUrl(ketchUrl);
        setVisibility(View.VISIBLE);
    }

    private static class PreferenceCenterJavascriptInterface {
        private final KetchWebView ketchWebView;

        private PreferenceCenterJavascriptInterface(KetchWebView ketchWebView) {
            this.ketchWebView = ketchWebView;
        }

        @JavascriptInterface
        public void hideExperience(@Nullable String status) {
            Log.d(TAG, String.format("hideExperience: %s", status));
            if (status != null && (status.equalsIgnoreCase(SET_CONSENT) || (status.equalsIgnoreCase(CLOSE)))) {
                runOnMainThread(() -> {
                    if (ketchWebView.listener != null) {
                        ketchWebView.listener.onClose();
                    }
                    ketchWebView.setVisibility(View.GONE);
                });
            }
        }

        @JavascriptInterface
        public void usprivacy_updated(@Nullable String ccpaString) {
            Log.d(TAG, String.format("onCCPAUpdate: %s", ccpaString));
            runOnMainThread(() -> {
                if (ketchWebView.listener != null) {
                    ketchWebView.listener.onCCPAUpdate(ccpaString);
                }
            });
        }

        @JavascriptInterface
        public void tcf_updated(@Nullable String tcfString) {
            Log.d(TAG, String.format("onTCFUpdate: %s", tcfString));
            runOnMainThread(() -> {
                ketchWebView.listener.onTCFUpdate(tcfString);
            });
        }

        @JavascriptInterface
        public void environment(@Nullable String environment) {
            Log.d(TAG, String.format("environment: %s", environment));
        }

        @JavascriptInterface
        public void regionInfo(@Nullable String regionInfo) {
            Log.d(TAG, String.format("regionInfo: %s", regionInfo));
        }

        @JavascriptInterface
        public void jurisdiction(@Nullable String jurisdiction) {
            Log.d(TAG, String.format("jurisdiction: %s", jurisdiction));
        }

        @JavascriptInterface
        public void identities(@Nullable String identities) {
            Log.d(TAG, String.format("identities: %s", identities));
        }

        @JavascriptInterface
        public void consent(@Nullable String consent) {
            Log.d(TAG, String.format("consent: %s", consent));
        }

        @JavascriptInterface
        public void showConsentExperience(@Nullable String showConsentExperience) {
            Log.d(TAG, String.format("showConsentExperience: %s", showConsentExperience));
        }

        @JavascriptInterface
        public void showPreferenceExperience(@Nullable String showPreferenceExperience) {
            Log.d(TAG, String.format("showPreferenceExperience: %s", showPreferenceExperience));
        }

        private void runOnMainThread(Runnable runnable) {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    interface KetchListener {
        void onCCPAUpdate(@Nullable String ccpaString);

        void onTCFUpdate(@Nullable String tcfString);

        void onClose();
    }
}
