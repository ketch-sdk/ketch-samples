# Ketch Smart Tag for the Android app

This document demonstrates the Ketch Smart Tag usage for the Kotlin based native Android application.

It handles the storage of the corresponding policy strings to SharedPreferences,
as per standards requirements for the in-app support:
- [IAB Europe Transparency & Consent Framework](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details)
- [CCPA Compliance Mechanism](https://github.com/InteractiveAdvertisingBureau/USPrivacy/blob/master/CCPA/USP%20API.md#in-app-support)

### Prerequisites
- Registered [Ketch organization account](https://app.ketch.com/settings/organization)
- Configured [application property](https://app.ketch.com/deployment/applications) record
- [Custom identity space](https://docs.ketch.com/hc/en-us/articles/360063594173-Managing-Properties#configuring-data-layer-setup-0-9)
- [index.html](./app/src/main/assets/index.html) Ketch Smart Tag integration bridge and [Kotlin utility classes](./app/src/main/java/com/ketch/sample/pref/).

## Quick Start

Before we start, take a look at the [fully functional sample of Android app](./../Android%20Ketch%20Pref%20Center%20using%20Kotlin),
where the following steps are implemented, integrating the Ketch Smart Tag into your Kotlin based Android project.

### Step 1. Copy the integration bridge into the app

Create `[module]/app/src/main/assets` folder and put the [index.html](./app/src/main/assets/index.html) file there.

The `index.html` file makes use of Android `WebView` and `JavascriptInterface` to
communicate back and forth with the native runtime of the Android application.

### Step 2. Create the activity with the webview

Copy the following files to your module package:
- [KetchPrefCenterActivity](./app/src/main/java/com/ketch/sample/pref/KetchWebView.kt)
- [KetchSharedPreferences](./app/src/main/java/com/ketch/sample/pref/KetchSharedPreferences.kt)
- [Identity](./app/src/main/java/com/ketch/sample/pref/Identity.kt)

These helper classes cover the communication with the JavaScript SDK
running inside the webview and storage of the corresponding policy strings.

### Step 3. Initialize the activity and helper classes

Initialize the KetchSharedPreferences instance

```kotlin
class MainActivity : AppCompatActivity() {
    
    ...

    private val sharedPreferences: KetchSharedPreferences by lazy {
        KetchSharedPreferences(this)
    }

    ...
}
```

Add the private member for storing the advertising ID and loading routines.

[AAID](https://developer.android.com/training/articles/ad-id) is the suitable way to identify anonymous users,
and we use it in this example for the sake of simplicity.

There are other [ways to provide the user's identity information](https://docs.ketch.com/hc/en-us/articles/1500003453742-About-Identity-Spaces#field-descriptions-0-1).
Use email or any other identifier that is available in you app.

```kotlin
class MainActivity : AppCompatActivity() {

    ...

    private val advertisingId = MutableStateFlow<String?>(null)

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadAdvertisingId() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                advertisingId.value =
                    AdvertisingIdClient.getAdvertisingIdInfo(applicationContext).id
            } catch (e: Exception) {
                e.printStackTrace()
                progressBar.isVisible = false
                Toast.makeText(
                    this@MainActivity,
                    R.string.cannot_get_advertising_id_toast,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }

    ...

}
```

### Step 4. Finally, setup the Ketch webview with the Ketch JS SDK configuration

Construct the webview using the "onCreate" lifecycle method in your main activity.
In works in a similar way to The Ketch Smart Tag on the HTML page 
that expects an organization code and app property code to be passed in to it.

Organization code `ORG_CODE` could be found on the [Organization Settings](https://app.ketch.com/settings/organization)

App property code `PROPERTY` could be found on the [Properties Management](https://app.ketch.com/deployment/applications)

Advertising ID code `ADVERTISING_ID_KEY` is available on the [Identity Spaces](https://app.ketch.com/settings/identity-spaces)

The preferences screen is triggered on the button click in this example, but it could also be
triggered automatically on application start.

```kotlin
class MainActivity : AppCompatActivity() {

    ...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        ketchWebView.listener = object : KetchWebView.KetchListener {
            override fun onCCPAUpdate(ccpaString: String?) {
                sharedPreferences.saveUSPrivacyString(ccpaString)
            }

            override fun onTCFUpdate(tcfString: String?, tcfApplies: Int?) {
                sharedPreferences.saveTCFTCString(tcfString)
                sharedPreferences.saveTCFGdprApplies(tcfApplies)
            }

            override fun onSave() {
                mainLayout.isVisible = true
            }

            override fun onCancel() {
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

    // navigate to the Ketch Dashboard settings screen for these values
    companion object {
        private const val ORG_CODE = "XXXX-your-org-code-XXXX"
        private const val PROPERTY = "XXXX-your-property-tag-XXXX"
        private const val ADVERTISING_ID_CODE = "XXXX-your-advertising-id-code-XXXX"
    }
    
    ...
}

```

Now when you run your application and have the Ketch account properly setup,
you will see the corresponding policy strings added to your default SharedPreferences.

### Done!
