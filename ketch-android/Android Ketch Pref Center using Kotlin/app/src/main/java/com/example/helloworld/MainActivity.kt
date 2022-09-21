package com.example.helloworld

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private val userEmail = "android@test.com"

    private val consentSharedPreferences: ConsentSharedPreferences by lazy {
        ConsentSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    intent?.getParcelableExtra<Consent>(KetchPrefCenter.CONSENT_KEY)?.let {
                        Log.d(TAG, "Consent: ")
                        it.purposes.forEach {
                            Log.d(TAG, " ${it.key} = ${it.value}")
                        }

                        consentSharedPreferences.put(userEmail, it)
                    }
                }
            }

        val buttonClick = findViewById<Button>(R.id.button)
        buttonClick.setOnClickListener {

            // identities to be passed to the WebView displaying the Ketch Preference Center
            val identities = ArrayList<Identity>()
            identities.add(Identity(VISITOR_ID_KEY, userEmail))

            val intent = Intent(this, KetchPrefCenter::class.java)
            intent.putExtra(IDENTITIES_KEY, identities)
            intent.putExtra(ORG_CODE_KEY, "transcenda")
            intent.putExtra(PROPERTY_KEY, "website_smart_tag")
            startForResult.launch(intent)
        }
    }

    companion object {
        private val TAG = KetchPrefCenter::class.java.simpleName

        const val VISITOR_ID_KEY = "visitorId"
        const val IDENTITIES_KEY = "identities"
        const val ORG_CODE_KEY = "orgCode"
        const val PROPERTY_KEY = "property"
    }
}
