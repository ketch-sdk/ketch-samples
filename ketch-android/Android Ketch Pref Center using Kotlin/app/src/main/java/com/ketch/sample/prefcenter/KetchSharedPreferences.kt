package com.ketch.sample.prefcenter

import android.content.Context
import android.content.SharedPreferences


class KetchSharedPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    fun getUSPrivacyString(): String? = sharedPreferences.getString(IAB_US_PRIVACY_STRING, null)

    fun getTCFTcString(): String? = sharedPreferences.getString(IAB_TCF_TC_STRING, null)

    fun getTCFGdprApplies(): Int? = if (sharedPreferences.contains(IAB_TCF_GDPR_APPLIES)) {
        sharedPreferences.getInt(IAB_TCF_GDPR_APPLIES, 0)
    } else null

    fun save(consent: Consent) {
        with(sharedPreferences.edit()) {
            putString(IAB_US_PRIVACY_STRING, consent.IABUSPrivacy_String)
            putString(IAB_TCF_TC_STRING, consent.IABTCF_TCString)

            consent.IABTCF_gdprApplies?.let {
                putInt(IAB_TCF_GDPR_APPLIES, it)
            } ?: remove(IAB_TCF_GDPR_APPLIES)

            apply()
        }
    }

    fun clear() {
        with(sharedPreferences.edit()) {
            remove(IAB_US_PRIVACY_STRING)
            remove(IAB_TCF_TC_STRING)
            remove(IAB_TCF_GDPR_APPLIES)

            apply()
        }
    }

    companion object {
        private const val IAB_TCF_TC_STRING = "IABTCF_TCString"
        private const val IAB_US_PRIVACY_STRING = "IABUSPrivacy_String"
        private const val IAB_TCF_GDPR_APPLIES = "IABTCF_gdprApplies"
    }
}
