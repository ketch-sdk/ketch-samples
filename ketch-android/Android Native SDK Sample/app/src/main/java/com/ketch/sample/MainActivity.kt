package com.ketch.sample

import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.ketch.android.Ketch
import com.ketch.android.KetchSdk
import com.ketch.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val advertisingId = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadAdvertisingId(binding)

        collectState(advertisingId) {
            with(binding) {
                progressBar.isVisible = false
            }
            it?.let {
                val ketch = setupKetch(it)
                setupButtons(ketch)
            }
        }
    }

    private fun setupButtons(ketch: Ketch) {
        with(binding) {
            buttonShowBanner.isVisible = true
            buttonShowBanner.setOnClickListener {
                ketch.showBanner(supportFragmentManager)
            }
        }
    }

    private fun setupKetch(advertisingId: String): Ketch = KetchSdk.create(
        organization = ORGANIZATION,
        property = PROPERTY,
        environment = ENVIRONMENT,
        controller = CONTROLLER,
        advertisingIdCode = advertisingId,
        gdpr = GDPR,
        ccpa = CCPA
    )

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadAdvertisingId(binding: ActivityMainBinding) {
        binding.progressBar.isVisible = true
        GlobalScope.launch(Dispatchers.IO) {
            try {
                advertisingId.value =
                    AdvertisingIdClient.getAdvertisingIdInfo(applicationContext).id
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
                    binding.progressBar.isVisible = false
                }
            }
        }
    }

    companion object {
        private const val ORGANIZATION = "<organization code>"
        private const val PROPERTY = "<property>"
        private const val ENVIRONMENT = "<environment>"
        private const val CONTROLLER = "<controller>"
        private const val ADVERTISING_ID_CODE = "<advertising code>"
        private const val GDPR = "gdpr"
        private const val CCPA = "ccpa"
    }
}
