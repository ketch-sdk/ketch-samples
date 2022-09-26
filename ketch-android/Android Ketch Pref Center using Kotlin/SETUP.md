# Ketch Smart Tag for the Android app

This documentation demonstrates the Ketch Smart Tag usage for the Kotlin based Android application.

## Prerequisites
- Registered [Ketch organization account](https://app.ketch.com/settings/organization) 
- Configured [application property](https://app.ketch.com/deployment/applications) record
- [Custom identity space](https://docs.ketch.com/hc/en-us/articles/360063594173-Managing-Properties#configuring-data-layer-setup-0-9)
- [index.html](./app/src/main/assets/index.html) Ketch Smart Tag integration bridge

## Quick Start

To integrate the Ketch Smart Tag into your Kotlin based Android project follow these steps:

### 1. Copy the integration bridge into an app  

Create `[module]/app/src/main/assets` folder and put the [index.html](./app/src/main/assets/index.html) file there.

The `index.html` file makes use of Android `WebView` and `JavascriptInterface` to 
communicate back and forth with the native runtime of the Android application.

### 2. Create the Ketch Preferences Center activity with the webview

In your application in Android Studio, copy the following files to your package:
- [KetchPrefCenter](./app/src/main/java/com/ketch/sample/prefcenter/KetchPrefCenterActivity.kt)
- [KetchSharedPreferences](./app/src/main/java/com/ketch/sample/prefcenter/KetchSharedPreferences.kt)
- [Consent](./app/src/main/java/com/ketch/sample/prefcenter/Consent.kt)
- [Identity](./app/src/main/java/com/ketch/sample/prefcenter/Identity.kt)

The activity and helper classes cover the communication with the JavaScript SDK 
running inside the webview and storage of the corresponding policy strings as per []() 



- To keep the activity as configurable as the Ketch Smart Tag on the HTML page, it expects an organization code and property code to be passed in to it. Retrieve these parameters and construct the url for the HTML page created earlier.
```kotlin
class KetchPrefCenter: AppCompatActivity() {
    override fun onCreate(savedInstance: Bundle?) {

        ...

        //pass in the property code and  to be used with the Ketch Smart Tag
        var ketchProperty = intent.getStringExtra("property")
        var ketchOrgCode = intent.getStringExtra("orgCode")
        var url = "https://appassets.androidplatform.net/assets/index.html?orgCode=$ketchOrgCode&propertyName=$ketchProperty"
        
    }
}
```

- In addition to passing in the organization code and property code, the activity will take in a list of identites for the current apps user. Retrieve the list of identities, encode them, and attach them to the url.
```kotlin
class KetchPrefCenter: AppCompatActivity() {
    override fun onCreate(savedInstance: Bundle?) {

        ...

        var identitiesJSON = JSONObject()
        if (identities != null) {
            for(identity in identities){
                identitiesJSON.put(identity.code, identity.value)
            }
        }

        var encodedIdentities = Base64.encodeToString(identitiesJSON.toString().toByteArray(), Base64.DEFAULT);
        url = "$url&encodedIdentities=$encodedIdentities"
        
    }
}
```

- With the url now complete, load it into the web view.
```kotlin
class KetchPrefCenter: AppCompatActivity() {
    override fun onCreate(savedInstance: Bundle?) {

        ...

        myWebView.loadUrl(url)
        
    }
}

```

- To know when the Ketch Preference Center has been closed, the HTML page will send a message to a JavaScript interface. Create a new class and annotate it to allow exposing methods to JavaScript. Check if the `PreferenceCenterClosed` message has been received and close the activity if it has. 
```kotlin
class PreferenceCenterJavascriptInterface(private val context: Context) {
    @JavascriptInterface
    fun receiveMessage(message: String) {
        if (message == "PreferenceCenterClosed") {
            (context as? KetchPrefCenter)?.finish()
        }
    }
}
```

- Add the Preference Center JavaScript Interface to web view
```kotlin
class KetchPrefCenter: AppCompatActivity() {
    override fun onCreate(savedInstance: Bundle?) {

        ...

        myWebView.addJavascriptInterface(PreferenceCenterJavascriptInterface(this), "androidListener")
        
    }
}
```

- In addition to sending back when the Ketch Preference Center is closed, the HTML page created above also sends back the consent preferences for the user. To handle this information, create a class to deserialize the data passed back from the HTML page and a JavaScript interface to receive the message.
```kotlin
class KetchPrefCenter : AppCompatActivity() {
    var consent:Consent? = null

    ...

}

class UserConsentJavascriptInterface(private val context: Context) {
    @JavascriptInterface
    fun receiveMessage(message: String) {
        (context as? KetchPrefCenter)?.consent = Consent(message)
    }
}

class Consent(json: String) : JSONObject(json), Serializable {
    val purposes = Purposes(this.getString("purposes"))
}

class Purposes(json: String) : JSONObject(json), Serializable {
    val analytics: Boolean = this.optBoolean("analytics")
}
```

## Launching Ketch Preference Center activity

Now that we've got the Ketch Preference Center activity created, let's see how to use it.

- Inside your Android app, add a new button to navigate to the Ketch Preference Center activity. 

- In the `onCreate` function, wire get a reference to the button and add an on-click listener.
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        
        ...

        val buttonClick = findViewById<Button>(R.id.button)
        buttonClick.setOnClickListener {

        }
    }
}
```

- Because we expect to receive the users consent preferences back from the activity, create a a way to register to receive a result from the.
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        
        ...

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                { 
                    ...
                }
            }
            
            ...

    }
}
``` 

- Next, add a place to store the consent and retrieve it from the result.
```kotlin
class MainActivity : AppCompatActivity() {
    var consent:Consent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        
        ...

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    consent = (intent?.getSerializableExtra("consent") as? Consent)
                }
            }

        }
            
        ...

    }
}
``` 

- Now it's time to start the Ketch Preference Center activity. Inside the button click listener, create the intent to start the activity, passing in the organization code and property code.
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        
        ...

        buttonClick.setOnClickListener {
            val intent = Intent(this, KetchPrefCenter::class.java)
            intent.putExtra("property", "web")
            intent.putExtra("orgCode", "your_org_code_here")
        }
    }
}
```

To ensure the customer's consent choices move with them no matter what device they use to interact with your brand, we can send identity information already present in the app to the Ketch Smart Tag running on the HTML page.

The HTML page we created earlier is already expecting and checking for identity information in query parameters of the URL. So, now all we need to do is append these to our URL.

- Start by creating and `Identity` class ensuring it is marked as serializable so they can be added to the intent and passed to the activity.
```kotlin
class Identity(val code: String, val value: String) : Serializable {
}
```

- Using the new identities class, create an array to store as many identifiers you may have for the user. In the example, we'll hard code an email address, but a device identifier, account identifier, and/or any other identifier can be added and used to identify a store a users consent preferences against. 
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        
        ...

        buttonClick.setOnClickListener {
            val identities = ArrayList<Identity>()
            identities.add(Identity("visitorId", "android@test.com"))

            val intent = Intent(this, KetchPrefCenter::class.java)
            intent.putExtra("identities", identities)

            ...

        }
    }
}
```

- Finally, launch the activity.
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        
        ...

        buttonClick.setOnClickListener {
            
            ...

            val intent = Intent(this, KetchPrefCenter::class.java)

            ...

            startForResult.launch(intent);
        }
    }
}
```

Now when you test your application and have set a consent state, you will be able to search the Ketch Audit Logs by the identity your passing through to the `dataLayer` within the HTML page.