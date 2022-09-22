package com.ketch.sample.prefcenter

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson


class ConsentSharedPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun get(userId: String): Consent? {
        return sharedPreferences.getString(userId, null)?.let {
            Gson().fromJson(it, Consent::class.java)
        }
    }

    fun put(userId: String, consent: Consent) {
        Gson().toJson(consent).apply {
            sharedPreferences.edit().putString(userId, this).apply()
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_NAME = "ketch_consent"
    }
}
