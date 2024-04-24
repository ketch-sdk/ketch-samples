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

Before we start, take a look at the [fully functional sample of Android app](./../Android%20Ketch%20Pref%20Center%20using%20Java),
where the following steps are implemented, integrating the Ketch Smart Tag into your Kotlin based Android project.

### Step 1. Copy the integration bridge into the app

Create `[module]/app/src/main/assets` folder and put the [index.html](./app/src/main/assets/index.html) file there.

The `index.html` file makes use of Android `WebView` and `JavascriptInterface` to
communicate back and forth with the native runtime of the Android application.

### Step 2. Create the activity with the webview

Copy the following files to your module package:
- [KetchWebView](./app/src/main/java/com/ketch/sample/pref/KetchWebView.java)
- [KetchSharedPreferences](./app/src/main/java/com/ketch/sample/pref/KetchSharedPreferences.java)
- [Identity](./app/src/main/java/com/ketch/sample/pref/Identity.java)

These helper classes cover the communication with the JavaScript SDK
running inside the webview and storage of the corresponding policy strings.

### Step 3. Initialize the activity and helper classes

Initialize the KetchSharedPreferences instance

```java
public class MainActivity extends AppCompatActivity {
    ...
    
    private KetchSharedPreferences sharedPreferences;

    ...
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ...
        
        sharedPreferences = new KetchSharedPreferences(getApplicationContext());
        
        ...

```

Add the private member for storing the advertising ID and loading routines.

[AAID](https://developer.android.com/training/articles/ad-id) is the suitable way to identify anonymous users,
and we use it in this example for the sake of simplicity.

There are other [ways to provide the user's identity information](https://docs.ketch.com/hc/en-us/articles/1500003453742-About-Identity-Spaces#field-descriptions-0-1).
Use email or any other identifier that is available in you app.

```java
public class MainActivity extends AppCompatActivity {

    ...

    private interface AdvertisingIdCallback {
        void onAdvertisingIdResult(@Nullable String advertisingId);
    }

    private void loadAdvertisingId(AdvertisingIdCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            String id = null;
            try {
                id = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this).getId();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                handler.post(() -> {
                    Toast.makeText(MainActivity.this,
                            R.string.cannot_get_advertising_id_toast,
                            Toast.LENGTH_LONG
                    ).show();
                });
            } finally {
                final String advertisingId = id;
                handler.post(() -> {
                    callback.onAdvertisingIdResult(advertisingId);
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    ...

}
```

### Step 4. Finally, setup the Ketch webview with the Ketch JS SDK configuration

Create the webview class or use KetchWebView from this sample. Add ths view to your your main activity layout.
In works in a similar way to The Ketch Smart Tag on the HTML page
that expects an organization code and app property code to be passed in to it.

Organization code `ORG_CODE` could be found on the [Organization Settings](https://app.ketch.com/settings/organization)

App property code `PROPERTY` could be found on the [Properties Management](https://app.ketch.com/deployment/applications)

Advertising ID code `ADVERTISING_ID_KEY` is available on the [Identity Spaces](https://app.ketch.com/settings/identity-spaces)

The preferences screen is triggered on the button click in this example, but it could also be
triggered automatically on application start.

```java
public class MainActivity extends AppCompatActivity {

    private static final String ORG_CODE = "<organization code>";
    private static final String PROPERTY = "<property>";
    private static final String ADVERTISING_ID_CODE = "<advertising field code>";
    ...

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        sharedPreferences = new KetchSharedPreferences(getApplicationContext());

        ketchWebView = findViewById(R.id.ketchWebView);
        mainLayout = findViewById(R.id.mainLayout);
        button = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);

        ketchWebView.setListener(new KetchWebView.KetchListener() {
            @Override
            public void onCCPAUpdate(String ccpaString) {
                sharedPreferences.saveUSPrivacyString(ccpaString);
            }

            @Override
            public void onTCFUpdate(String tcfString, int tcfApplies) {
                sharedPreferences.saveTCFTCString(tcfString);
                sharedPreferences.saveTCFGdprApplies(tcfApplies);
            }

            @Override
            public void onSave() {
                mainLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancel() {
                mainLayout.setVisibility(View.GONE);
            }
        });

        button.setOnClickListener(v -> {
            ketchWebView.show();
            mainLayout.setVisibility(View.GONE);
        });

        loadAdvertisingId(advertisingId -> {
            if (advertisingId != null) {
                button.setVisibility(View.VISIBLE);

                // identities to be passed to the WebView displaying the Ketch Preference Center
                List<Identity> identities = new ArrayList<>();
                identities.add(new Identity(ADVERTISING_ID_CODE, advertisingId));

                ketchWebView.init(ORG_CODE, PROPERTY, identities);
            } else {
                button.setVisibility(View.GONE);
            }
        });
    }

    ...
}

```

Now when you run your application and have the Ketch account properly setup,
you will see the corresponding policy strings added to your default SharedPreferences.

### Done!
