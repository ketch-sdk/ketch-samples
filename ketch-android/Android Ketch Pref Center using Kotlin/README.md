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
- [KetchPrefCenterActivity](./app/src/main/java/com/ketch/sample/pref/KetchPrefCenterActivity.kt)
- [KetchSharedPreferences](./app/src/main/java/com/ketch/sample/pref/KetchSharedPreferences.kt)
- [Consent](./app/src/main/java/com/ketch/sample/pref/Consent.kt)
- [Identity](./app/src/main/java/com/ketch/sample/pref/Identity.kt)

The activity and helper classes cover the communication with the JavaScript SDK
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

```kotlin
class MainActivity : AppCompatActivity() {

    ...

    private var advertisingId: String? = null
    
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

    ...

}
```

### Step 4. Setup the intent with the Ketch JS SDK configuration

Construct the Intent to keep the activity configurable similar to The Ketch Smart Tag
on the HTML page that expects an organization code and app property code to be passed in to it.

Organization code `ORG_CODE` could be found on the [Organization Settings](https://app.ketch.com/settings/organization)

App property code `PROPERTY` could be found on the [Properties Management](https://app.ketch.com/deployment/applications)

Advertising ID code `ADVERTISING_ID_KEY` is available on the [Identity Spaces](https://app.ketch.com/settings/identity-spaces)

```kotlin
class MainActivity : AppCompatActivity() {

    ...

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

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val ORG_CODE = "your organization code"
        private const val PROPERTY = "your app property code"
        private const val ADVERTISING_ID_KEY = "advertising ID field code"
    }
}

```

### Step 5. Finally, launch the activity and trigger preferences popup

Create the activity result handler and add the trigger for the webview.

The preferences activity is triggered on the button click in this example, but it could also be
triggered automatically on application start.

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ...

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

        ...

    }
}
```

Now when you run your application and have set a consent state,
you will see the corresponding policy strings added to your default SharedPreferences.

### Done!
