package com.ketch.sample

import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.ketch.android.KetchNotification
import com.ketch.android.KetchSdk
import com.ketch.android.api.request.PurposeAllowedLegalBasis
import com.ketch.android.api.request.User
import com.ketch.android.api.response.ErrorResult
import com.ketch.android.ccpa.CCPAPlugin
import com.ketch.android.tcf.TCFPlugin
import com.ketch.android.ui.KetchUi
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
            binding.ketchLayout.isVisible = it != null
            it?.let { setupKetch(it) }
        }
    }

    private fun setupKetch(advertisingId: String) {
        val ketch = KetchSdk.create(
            organization = ORGANIZATION,
            property = PROPERTY,
            environment = ENVIRONMENT,
            controller = CONTROLLER,
            identities = mapOf(ADVERTISING_ID_CODE to advertisingId)
        )

        val preferenceService = PreferenceService(this)

        val ccpaPlugin = CCPAPlugin { encodedString, applied ->
            preferenceService.saveUSPrivacyString(encodedString, applied)
        }.apply {
            notice = true
            lspa = true
        }

        val tcfPlugin = TCFPlugin { encodedString, applied ->
            preferenceService.saveTCFTCString(encodedString, applied)
        }

        ketch.addPlugins(
            ccpaPlugin,
            tcfPlugin
        )

        val ketchUi = KetchUi(this, ketch)

        collectState(ketch.loading) {
            binding.progressBar.isVisible = it
        }

        collectState(ketch.configuration) {
            binding.buttonUpdateConsent.isVisible = it != null
            binding.buttonGetConsent.isVisible = it != null
            binding.buttonInvokeRights.isVisible = it != null
        }

        collectState(ketch.consent) {
            binding.buttonShowBanner.isVisible = it != null
            binding.buttonShowModal.isVisible = it != null
            binding.buttonShowJit.isVisible = it != null
            binding.buttonShowPreference.isVisible = it != null
        }

        ketch.notifications.collectLifecycle {
            when (it) {
                is KetchNotification.KetchNotificationSuccess -> {}
                is KetchNotification.KetchNotificationError -> {
                    val message = when (val errorResult = it.error) {
                        is ErrorResult.HttpError -> errorResult.error.errMessage
                        is ErrorResult.Offline -> getString(R.string.device_offline)
                        is ErrorResult.ServerNotAvailable -> getString(R.string.server_not_available)
                        is ErrorResult.OtherError -> errorResult.throwable.message
                    }
                    Toast.makeText(
                        this@MainActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        binding.checkboxShowDialogsAutomatically.setOnCheckedChangeListener { _, isChecked ->
            ketchUi.showDialogsIfNeeded = isChecked
        }

        binding.buttonGetFullConfiguration.setOnClickListener {
            ketch.loadConfiguration()
        }

        binding.buttonGetGDPRConfiguration.setOnClickListener {
            ketch.loadConfiguration(
                jurisdiction = GDPR
            )
        }

        binding.buttonGetCCPAConfiguration.setOnClickListener {
            ketch.loadConfiguration(
                jurisdiction = CCPA
            )
        }

        binding.buttonInvokeRights.setOnClickListener {
            val configuration = ketch.configuration.value

            configuration?.rights?.firstOrNull()?.code?.let { right ->
                ketch.invokeRights(
                    right = right,
                    user = User(
                        email = "rkuziv@transcenda.com",
                        first = "Roman",
                        last = "Kuziv",
                        country = "Ukraine",
                        stateRegion = "Kyiv",
                        description = null,
                        phone = null,
                        postalCode = null,
                        addressLine1 = null,
                        addressLine2 = null,
                    )
                )
            }
        }

        binding.buttonGetConsent.setOnClickListener {
            ketch.loadConsent()
        }

        binding.buttonUpdateConsent.setOnClickListener {
            val configuration = ketch.configuration.value

            ketch.updateConsent(
                purposes = configuration?.purposes?.map {
                    it.code to PurposeAllowedLegalBasis(
                        allowed = true.toString(),
                        legalBasisCode = "disclosure"
                    )
                }?.toMap() ?: emptyMap(),
                vendors = configuration?.vendors?.map {
                    it.id
                },
            )
        }

        binding.buttonShowBanner.setOnClickListener {
            val configuration = ketch.configuration.value
            val consent = ketch.consent.value

            if (configuration == null || consent == null) return@setOnClickListener

            ketchUi.showBanner(configuration, consent)
        }

        binding.buttonShowModal.setOnClickListener {
            val configuration = ketch.configuration.value
            val consent = ketch.consent.value

            if (configuration == null || consent == null) return@setOnClickListener

            ketchUi.showModal(configuration, consent)
        }

        binding.buttonShowJit.setOnClickListener {
            val configuration = ketch.configuration.value
            val consent = ketch.consent.value
            val purpose = configuration?.purposes?.firstOrNull()

            if (configuration == null || consent == null || purpose == null) return@setOnClickListener

            ketchUi.showJit(configuration, consent, purpose)
        }

        binding.buttonShowPreference.setOnClickListener {
            val configuration = ketch.configuration.value
            val consent = ketch.consent.value

            if (configuration == null || consent == null) return@setOnClickListener

            ketchUi.showPreference(configuration, consent)
        }
    }

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
