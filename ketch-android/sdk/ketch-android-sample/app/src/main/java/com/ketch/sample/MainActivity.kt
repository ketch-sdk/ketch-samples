package com.ketch.sample

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.Gson
import com.ketch.android.Ketch
import com.ketch.android.KetchSdk
import com.ketch.android.data.Consent
import com.ketch.android.data.HideExperienceStatus
import com.ketch.android.data.KetchConfig
import com.ketch.android.data.WillShowExperienceType
import com.ketch.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val advertisingId = MutableStateFlow<String?>(null)

    private val languages = arrayOf(SYSTEM, "en", "fr")
    private val jurisdictions = arrayOf("default", "gdpr")
    private val regions = arrayOf("US", "FR", "GB", "JM")

    private val listener = object : Ketch.Listener {

        override fun onEnvironmentUpdated(environment: String?) {
            Log.d(TAG, "onEnvironmentUpdated: environment = $environment")
        }

        override fun onRegionInfoUpdated(regionInfo: String?) {
            Log.d(TAG, "onRegionInfoUpdated: regionInfo = $regionInfo")
        }

        override fun onShow() {
            Log.d(TAG, "onShow")
        }

        override fun onJurisdictionUpdated(jurisdiction: String?) {
            Log.d(TAG, "onJurisdictionUpdated: jurisdiction = $jurisdiction")
        }

        override fun onIdentitiesUpdated(identities: String?) {
            Log.d(TAG, "onIdentitiesUpdated: identities = $identities")
        }

        override fun onConfigUpdated(config: KetchConfig?) {
            val configJson = Gson().toJson(config)
            Log.d(TAG, "onConfigUpdated: config = $configJson")
        }

        override fun onConsentUpdated(consent: Consent) {
            val consentJson = Gson().toJson(consent)
            Log.d(TAG, "onConsentUpdated: consent = $consentJson")
        }

        override fun onDismiss(status: HideExperienceStatus) {
            Log.d(TAG, "onDismiss: status = ${status.name}")
        }

        override fun onError(errMsg: String?) {
            Log.e(TAG, "onError: errMsg = $errMsg")
        }

        override fun onUSPrivacyUpdated(values: Map<String, Any?>) {
            Log.d(TAG, "onUSPrivacyUpdated: $values")
        }

        override fun onTCFUpdated(values: Map<String, Any?>) {
            Log.d(TAG, "onTCFUpdated: $values")
        }

        override fun onGPPUpdated(values: Map<String, Any?>) {
            Log.d(TAG, "onGPPUpdated: $values")
        }

        override fun onWillShowExperience(type: WillShowExperienceType) {
            Log.d(TAG, "onWillShowExperience: type = ${type.name}")
        }

        override fun onHasShownExperience() {
            Log.d(TAG, "hasShownExperience")
        }
    }

    private val ketch: Ketch by lazy {
        // Create the KetchSDK object
        KetchSdk.create(
            this,
            supportFragmentManager,
            // Replace below with your Ketch organization code
            ORG_CODE,
            // Replace below with your Ketch property code
            PROPERTY,
            null,
            listener,
            TEST_URL,
            Ketch.LogLevel.DEBUG
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView) { container, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            container.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }

        setupUI()

        setParameters()

        loadAdvertisingId(binding)

        collectState(advertisingId) {
            with(binding) {
                progressBar.isVisible = false
            }
            it?.let {
                with(ketch) {
                    setIdentities(mapOf(ADVERTISING_ID_CODE to it))
                    load()
                }
            }
        }
    }

    private fun setupUI() {
        with(binding) {
            orgcode.setText(getString(R.string.org_code, ORG_CODE))
            property.setText(getString(R.string.property, PROPERTY))
            ketchurl.setText(getString(R.string.ketch_url, TEST_URL))

            val languageAdapter: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    languages
                )
            spLanguage.adapter = languageAdapter

            val jurisdictionAdapter: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    jurisdictions
                )
            spJurisdiction.adapter = jurisdictionAdapter

            val regionAdapter: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    regions
                )
            spRegion.adapter = regionAdapter

            buttonSetParameters.setOnClickListener {
                setParameters()
                ketch.load()
            }

            buttonShowPreferences.setOnClickListener {
                ketch.showPreferences()
            }

            val preferenceTabAdapter: ArrayAdapter<Ketch.PreferencesTab> =
                ArrayAdapter<Ketch.PreferencesTab>(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item
                )
            spPreferencesTab.adapter = preferenceTabAdapter

            val cbListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                val preferencesTabs = getMultiselectedPreferencesTab()
                val selectedItem = spPreferencesTab.selectedItemPosition.let {
                    if (it >= 0) {
                        preferenceTabAdapter.getItem(it)
                    } else null
                }
                preferenceTabAdapter.clear()
                preferenceTabAdapter.addAll(preferencesTabs)
                selectedItem?.let {
                    val index = preferencesTabs.indexOf(it)
                    if (index >= 0) {
                        spPreferencesTab.setSelection(index)
                    }
                }
            }
            cbOverviewTab.setOnCheckedChangeListener(cbListener)
            cbRightsTab.setOnCheckedChangeListener(cbListener)
            cbConsentsTab.setOnCheckedChangeListener(cbListener)
            cbSubscriptionsTab.setOnCheckedChangeListener(cbListener)

            buttonShowPreferencesTab.setOnClickListener {
                val multiselectedTabs = getMultiselectedPreferencesTab()
                if (multiselectedTabs.isNotEmpty()) {
                    spPreferencesTab.selectedItemPosition.let {
                        preferenceTabAdapter.getItem(it)
                    }?.let {
                        ketch.showPreferencesTab(multiselectedTabs, it)
                    }
                }
            }

            buttonShowConsent.setOnClickListener {
                ketch.showConsent()
            }

            buttonShowSharedPreferences.setOnClickListener {
                sharedPreferencesString.text = getSharedPreferencesString() ?: ""
            }
        }
    }

    private fun setParameters() {
        with(binding) {
            spLanguage.selectedItemPosition.let {
                languages[it].apply {
                    if (this != SYSTEM) {
                        ketch.setLanguage(this)
                    } else {
                        ketch.setLanguage(null)
                    }
                }
            }
            spJurisdiction.selectedItemPosition.let {
                ketch.setJurisdiction(jurisdictions[it])
            }
            spRegion.selectedItemPosition.let {
                ketch.setRegion(regions[it])
            }
        }
    }

    private fun getMultiselectedPreferencesTab(): List<Ketch.PreferencesTab> {
        val tabs = mutableListOf<Ketch.PreferencesTab>()
        if (binding.cbOverviewTab.isChecked) {
            tabs.add(Ketch.PreferencesTab.OVERVIEW)
        }
        if (binding.cbRightsTab.isChecked) {
            tabs.add(Ketch.PreferencesTab.RIGHTS)
        }
        if (binding.cbConsentsTab.isChecked) {
            tabs.add(Ketch.PreferencesTab.CONSENTS)
        }
        if (binding.cbSubscriptionsTab.isChecked) {
            tabs.add(Ketch.PreferencesTab.SUBSCRIPTIONS)
        }
        return tabs
    }

    private fun getSharedPreferencesString(): String? =
        when (binding.rgSharedPreferences.checkedRadioButtonId) {
            R.id.rbTCF -> ketch.getTCFTCString()
            R.id.rbUSPrivacy -> ketch.getUSPrivacyString()
            R.id.rbGPP -> ketch.getGPPHDRGppString()
            else -> null
        }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadAdvertisingId(binding: ActivityMainBinding) {
        binding.progressBar.isVisible = true
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
                    binding.progressBar.isVisible = false
                }
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val SYSTEM = "<SYSTEM>"

        private const val ORG_CODE = "ketch_samples"
        private const val PROPERTY = "android"
        private const val ADVERTISING_ID_CODE = "aaid"

        private const val TEST_URL = "https://global.ketchcdn.com/web/v3"
    }
}
