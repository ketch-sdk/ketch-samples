package com.ketch.sample.prefcenter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.ketch.sample.prefcenter.KetchPrefCenterActivity.Companion.IDENTITIES_KEY
import com.ketch.sample.prefcenter.KetchPrefCenterActivity.Companion.ORG_CODE_KEY
import com.ketch.sample.prefcenter.KetchPrefCenterActivity.Companion.PROPERTY_KEY
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val sharedPreferences: KetchSharedPreferences by lazy {
        KetchSharedPreferences(this)
    }

    private var advertisingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        loadAdvertisingId()

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "***** RESULT *****")
                    Log.d(TAG, "IABUSPrivacy_String: ${sharedPreferences.getUSPrivacyString()}")
                    Log.d(TAG, "IABTCF_TCString: ${sharedPreferences.getTCFTcString()}")
                    Log.d(TAG, "IABTCF_gdprApplies: ${sharedPreferences.getTCFGdprApplies()}")
                }
            }

        val buttonClick = findViewById<Button>(R.id.button)
        buttonClick.setOnClickListener {
            createKetchPrefCenterIntent()?.let { intent ->
                startForResult.launch(intent)
            }
        }
    }

    private fun createKetchPrefCenterIntent(): Intent? {
        if (advertisingId.isNullOrEmpty()) {
            Toast.makeText(this, R.string.cannot_get_advertising_id_toast, Toast.LENGTH_LONG)
                .show()
            return null
        }

        // identities to be passed to the WebView displaying the Ketch Preference Center
        val identities = ArrayList<Identity>()
        identities.add(Identity(ADVERTISING_ID_KEY, advertisingId!!))

        return Intent(this, KetchPrefCenterActivity::class.java).apply {
            putExtra(ORG_CODE_KEY, ORG_CODE)
            putExtra(PROPERTY_KEY, PROPERTY)
            putExtra(IDENTITIES_KEY, identities)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadAdvertisingId() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                advertisingId = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext).id
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val ORG_CODE = "transcenda"
        private const val PROPERTY = "website_smart_tag"
        private const val ADVERTISING_ID_KEY = "aaid"
    }
}
