package com.ketch.sample

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.ketch.android.Ketch
import com.ketch.sample.KetchApplication.Companion.ORG_CODE
import com.ketch.sample.KetchApplication.Companion.PROPERTY
import com.ketch.sample.KetchApplication.Companion.TEST_URL
import com.ketch.sample.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private val advertisingId = MutableStateFlow<String?>(null)

    private val languages = arrayOf(SYSTEM, "en", "fr")
    private val jurisdictions = arrayOf("default", "gdpr")
    private val regions = arrayOf("US", "FR", "GB", "JM")

    private val ketch: Ketch by lazy {
        (applicationContext as KetchApplication).ketch
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()

        loadAdvertisingId(binding)

        collectState(advertisingId) { aaid ->
            with(binding) {
                progressBar.isVisible = false
            }
            aaid?.let {
                with(ketch) {
                    setIdentities(mapOf(ADVERTISING_ID_CODE to it))
                    load()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ketch.setFragmentManager(supportFragmentManager)
    }

    override fun onStop() {
        ketch.stop()
        super.onStop()
    }

    private fun setupUI() {
        with(binding) {
            orgcode.text = getString(R.string.org_code, ORG_CODE)
            property.text = getString(R.string.property, PROPERTY)
            ketchurl.text = getString(R.string.ketch_url, TEST_URL)

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

    private fun loadAdvertisingId(binding: ActivityMainBinding) {
        binding.progressBar.isVisible = true
        lifecycleScope.launch(Dispatchers.IO) {
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
        private const val ADVERTISING_ID_CODE = "aaid"
        private const val SYSTEM = "<SYSTEM>"
    }
}
