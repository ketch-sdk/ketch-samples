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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var advertisingId: String? = null

    private val consentSharedPreferences: ConsentSharedPreferences by lazy {
        ConsentSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        loadAdvertisingId()

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    intent?.getParcelableExtra<Consent>(KetchPrefCenter.CONSENT_KEY)?.let {
                        Log.d(TAG, "Consent: ")
                        it.purposes.forEach {
                            Log.d(TAG, " ${it.key} = ${it.value}")
                        }

                        consentSharedPreferences.put("consent", it)
                    }
                }
            }

        val buttonClick = findViewById<Button>(R.id.button)
        buttonClick.setOnClickListener {
            if (advertisingId.isNullOrEmpty()) {
                Toast.makeText(this, R.string.cannot_get_advertising_id_toast, Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            // identities to be passed to the WebView displaying the Ketch Preference Center
            val identities = ArrayList<Identity>()
            identities.add(Identity(ADVERTISING_ID_KEY, advertisingId!!))

            val intent = Intent(this, KetchPrefCenter::class.java)
            intent.putExtra(IDENTITIES_KEY, identities)
            intent.putExtra(ORG_CODE_KEY, "transcenda")
            intent.putExtra(PROPERTY_KEY, "website_smart_tag")
            startForResult.launch(intent)
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
        private val TAG = KetchPrefCenter::class.java.simpleName

        const val ADVERTISING_ID_KEY = "aaid"
        const val IDENTITIES_KEY = "identities"
        const val ORG_CODE_KEY = "orgCode"
        const val PROPERTY_KEY = "property"
    }
}
