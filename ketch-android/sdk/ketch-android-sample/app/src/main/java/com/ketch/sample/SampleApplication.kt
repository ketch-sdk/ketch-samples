package com.ketch.sample

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.ketch.android.Ketch
import com.ketch.android.KetchSdk
import com.ketch.android.data.Consent
import com.ketch.android.data.HideExperienceStatus
import com.ketch.android.data.KetchConfig
import com.ketch.android.data.WillShowExperienceType

class SampleApplication : Application() {

    private val ketchListener = object : Ketch.Listener {

        override fun onEnvironmentUpdated(environment: String?) {
            Log.d(TAG, "onEnvironmentUpdated: environment = $environment")
        }

        override fun onRegionInfoUpdated(regionInfo: String?) {
            Log.d(TAG, "onRegionInfoUpdated: regionInfo = $regionInfo")
        }

        override fun onShow() {
            Log.d(TAG, "onShow")
        }

        override fun onJurisdictionUpdated(jurisdiction: String?) {
            Log.d(TAG, "onJurisdictionUpdated: jurisdiction = $jurisdiction")
        }

        override fun onIdentitiesUpdated(identities: String?) {
            Log.d(TAG, "onIdentitiesUpdated: identities = $identities")
        }

        override fun onConfigUpdated(config: KetchConfig?) {
            val configJson = Gson().toJson(config)
            Log.d(TAG, "onConfigUpdated: config = $configJson")
        }

        override fun onConsentUpdated(consent: Consent) {
            val consentJson = Gson().toJson(consent)
            Log.d(TAG, "onConsentUpdated: consent = $consentJson")
        }

        override fun onDismiss(status: HideExperienceStatus) {
            Log.d(TAG, "onDismiss: status = ${status.name}")
        }

        override fun onError(errMsg: String?) {
            Log.e(TAG, "onError: errMsg = $errMsg")
        }

        override fun onUSPrivacyUpdated(values: Map<String, Any?>) {
            Log.d(TAG, "onUSPrivacyUpdated: $values")
        }

        override fun onTCFUpdated(values: Map<String, Any?>) {
            Log.d(TAG, "onTCFUpdated: $values")
        }

        override fun onGPPUpdated(values: Map<String, Any?>) {
            Log.d(TAG, "onGPPUpdated: $values")
        }

        override fun onWillShowExperience(type: WillShowExperienceType) {
            Log.d(TAG, "onWillShowExperience: $type")
        }
    }

    val ketch: Ketch by lazy {
        // Create the KetchSDK object
        KetchSdk.create(
            this,
            // Replace below with your Ketch organization code
            ORG_CODE,
            // Replace below with your Ketch property code
            PROPERTY,
            null,
            ketchListener,
            TEST_URL,
            Ketch.LogLevel.DEBUG
        )
    }

    companion object {
        private val TAG = SampleApplication::class.java.simpleName

        const val TEST_URL = "https://global.ketchcdn.com/web/v3"

        const val ORG_CODE = "ketch_samples"
        const val PROPERTY = "android"
    }
}