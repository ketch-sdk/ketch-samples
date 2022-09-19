package com.example.helloworld

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize


class MainActivity : AppCompatActivity() {
    var consent:Consent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    consent = (intent?.getSerializableExtra("consent") as? Consent)

                }
            }

        val buttonClick = findViewById<Button>(R.id.button)
        buttonClick.setOnClickListener {

            // identities to be passed to the WebView displaying the Ketch Preference Center
            val identities = ArrayList<Identity>()
            identities.add(Identity("visitorId", "android@test.com"))

            val intent = Intent(this, KetchPrefCenter::class.java)
            intent.putExtra("identities", identities)
            intent.putExtra("property", "web")
            intent.putExtra("orgCode", "thatconf22_demo")
            startForResult.launch(intent);
        }
    }
}

@Parcelize
data class Identity(val code: String, val value: String): Parcelable