package com.ketch.sample

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


class PreferenceService(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    fun getUSPrivacyString(): String? = sharedPreferences.getString(IAB_US_PRIVACY_STRING, null)

    fun isUSPrivacyStringApplied(): Boolean? = if (sharedPreferences.contains(IAB_US_PRIVACY_APPLIED)) {
        sharedPreferences.getBoolean(IAB_US_PRIVACY_APPLIED, false)
    } else null

    fun saveUSPrivacyString(value: String?, applied: Boolean) {
        sharedPreferences.edit {
            value?.let {
                putString(IAB_US_PRIVACY_STRING, it)
                putBoolean(IAB_US_PRIVACY_APPLIED, applied)
            } ?: run {
                remove(IAB_US_PRIVACY_STRING)
                remove(IAB_US_PRIVACY_APPLIED)
            }
            apply()
        }
    }

    fun getTCFTCString(): String? = sharedPreferences.getString(IAB_TCF_TC_STRING, null)

    fun isTCFGdprApplied(): Boolean? = if (sharedPreferences.contains(IAB_TCF_GDPR_APPLIES)) {
        sharedPreferences.getBoolean(IAB_TCF_GDPR_APPLIES, false)
    } else null

    fun saveTCFTCString(value: String?, applied: Boolean) {
        sharedPreferences.edit {
            value?.let {
                putString(IAB_TCF_TC_STRING, it)
                putBoolean(IAB_TCF_GDPR_APPLIES, applied)
            } ?: run {
                remove(IAB_TCF_TC_STRING)
                remove(IAB_TCF_GDPR_APPLIES)
            }
            apply()
        }
    }

    companion object {
        private const val IAB_US_PRIVACY_STRING = "IABUSPrivacy_String"
        private const val IAB_US_PRIVACY_APPLIED = "IABUSPrivacy_Applied"

        private const val IAB_TCF_TC_STRING = "IABTCF_TCString"
        private const val IAB_TCF_GDPR_APPLIES = "IABTCF_gdprApplies"
    }
}
