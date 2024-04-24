package com.ketch.sample.pref

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStateAtLeast
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.android.synthetic.main.activity_main.button
import kotlinx.android.synthetic.main.activity_main.ketchWebView
import kotlinx.android.synthetic.main.activity_main.mainLayout
import kotlinx.android.synthetic.main.activity_main.progressBar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val sharedPreferences: KetchSharedPreferences by lazy {
        KetchSharedPreferences(this)
    }

    private val advertisingId = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        ketchWebView.listener = object : KetchWebView.KetchListener {
            override fun onCCPAUpdate(ccpaString: String?) {
                sharedPreferences.saveUSPrivacyString(ccpaString)
            }

            override fun onTCFUpdate(tcfString: String?) {
                sharedPreferences.saveTCFTCString(tcfString)
            }

            override fun onClose() {
                mainLayout.isVisible = true
            }
        }

        button.setOnClickListener {
            ketchWebView.show()
            mainLayout.isVisible = false
        }

        collectState(advertisingId) {
            button.isVisible = it != null

            it?.let { aaid ->
                progressBar.isVisible = false

                // identities to be passed to the WebView displaying the Ketch Preference Center
                val identities = ArrayList<Identity>()
                identities.add(Identity(ADVERTISING_ID_CODE, aaid))

                ketchWebView.init(ORG_CODE, PROPERTY, identities)
            }
        }

        progressBar.isVisible = true

        loadAdvertisingId()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadAdvertisingId() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                advertisingId.value = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext).id
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.cannot_get_advertising_id_toast,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                launch(Dispatchers.Main) {
                    progressBar.isVisible = false
                }
            }
        }
    }

    private fun <A> collectState(
        state: StateFlow<A>,
        minState: Lifecycle.State = Lifecycle.State.STARTED,
        collector: suspend (A) -> Unit
    ) = state.collectLifecycle(minState, collector)

    private fun <T> Flow<T>.collectLifecycle(
        minState: Lifecycle.State,
        collector: suspend (T) -> Unit
    ) {
        lifecycleScope.launch {
            lifecycle.whenStateAtLeast(minState) {
                collect {
                    collector(it)
                }
            }
        }
    }

    companion object {
        private const val ORG_CODE = "ketch_samples"
        private const val PROPERTY = "android"
        private const val ADVERTISING_ID_CODE = "<advertising field code>"
    }
}
