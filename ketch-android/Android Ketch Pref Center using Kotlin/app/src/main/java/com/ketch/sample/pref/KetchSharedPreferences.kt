package com.ketch.sample.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


class KetchSharedPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    fun getUSPrivacyString(): String? = sharedPreferences.getString(IAB_US_PRIVACY_STRING, null)

    fun saveUSPrivacyString(value: String?) {
        sharedPreferences.edit {
            value?.let {
                putString(IAB_US_PRIVACY_STRING, value)
            } ?: remove(IAB_US_PRIVACY_STRING)
            apply()
        }
    }

    fun getTCFTCString(): String? = sharedPreferences.getString(IAB_TCF_TC_STRING, null)

    fun saveTCFTCString(value: String?) {
        sharedPreferences.edit {
            value?.let {
                putString(IAB_TCF_TC_STRING, value)
            } ?: remove(IAB_TCF_TC_STRING)
            apply()
        }
    }

    companion object {
        private const val IAB_TCF_TC_STRING = "IABTCF_TCString"
        private const val IAB_US_PRIVACY_STRING = "IABUSPrivacy_String"
    }
}
