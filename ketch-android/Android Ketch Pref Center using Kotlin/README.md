# Using the Ketch Smart Tag within a Kotlin based Android project

This example demonstrates how you can use the Ketch Smart Tag within a Kotlin based Android application to display the preferences view. It also shows how to pass in an identity and add it to the dataLayer to be picked up by the Ketch Smart Tag.

The example utilizes an HTML page embedded into the application, but can easily be substituted to display a similar page hosted on a website. To display the page in the Android app, it utilizes the WebView from the Android WebKit library.


## Prerequisites
- A property configured and deployed within the Ketch Platform.
- A [custom identity space configured on the deployed property](https://docs.ketch.com/hc/en-us/articles/360063594173-Managing-Properties#configuring-data-layer-setup-0-9) to read it's value from the `dataLayer`.

## Getting Started

To get going, open your Kotlin based Android project, or create a new one.

Add an `assets` folder.

Next, we'll add a new file with the `Empty` template and name it `index.html`.

Inside the `index.html` file, add a standard webpage structure like below and set the `viewport` size for the page.

```html
<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
    </head>
    <body></body>
</html>
```

We'll be adding 3 scripts to the `head` of our page to do the following:
- Add the identities passed in through the query parameters to the `dataLayer`
- Load the Ketch Smart Tag
- Create a custom plugin to show the Ketch Preference Center and respond to the closing of the Ketch Preference Center

### Add identities to the `dataLayer`
The identities will be passed to the page through the query parameters of the URL. Once we have the identities, then we need to add them to the dataLayer, creating it first if it does not already exist. Add the following script to the `head` of the `index.html` page below the `<meta>` tag.

```html
<head>
    ...
    <script>
        // Get query parameters
        let params = (new URL(document.location)).searchParams;

        // Get identities from query string
        let encodedIdentities = params.get("encodedIdentities")
        let decodedIdentities = atob(encodedIdentities)

        // Add identities to dataLayer
        window.dataLayer = window.dataLayer || [];
        window.dataLayer.push(decodedIdentities);   
    </script>
    ...
</head>
```

In this example, we are expecting a `visitorId` to be passed in through the query parameters. This ID could be any identifier used to identify the user using the mobile app. It should also correspond to the identifier setup within the deployed Ketch Property on the Ketch Platform. 

## Load the Ketch Smart Tag

Traditionally, the Ketch Smart Tag will be [exported from the Ketch Property page](https://docs.ketch.com/hc/en-us/articles/360062451394-Tag-Implementation), but this example instead dynamically generates the necessary script, allowing the organization code and property code to be passed in through query parameters. 

```html
<head>
    ...
    <script>
        
        ...

        // Get property name from query parameters
        let propertyName = params.get("propertyName") || "web";

        // Add Ketch Smart Tag
        // Replace the org_code below with your organization code.
        var orgCode = 'thatconf22_demo';

        // Get organization code from query parameters
        //let propertyName = params.get("orgCode");

        var e=document.createElement("script");
        e.type="text/javascript";
        e.src=`https://global.ketchcdn.com/web/v1/config/${orgCode}/${propertyName}/boot.js`;
        e.defer=e.async=!0;
        document.getElementsByTagName("head")[0].appendChild(e);
        window.semaphore=window.semaphore||[];   
    </script>

    ...

</head>
```

_note: the Ketch Smart Tag script should be loaded ***AFTER*** the population of the identitied in the `dataLayer` this will ensure the Ketch Smart Tag will load the users previous consent choices. Providing a cohesive experience with your brand across their various platforms (web, mobile, etc.)_

### Create the custom plugin
Finally, we'll need to create a custom plugin to show and listen for the closing of the Ketch Preference Center.

When the Ketch Preference Center experience closes, the `experienceHidden` event is fired. The event will pass a reason for the experience being closed. The reason will either be a string containing the value `setConsent`, if a submit button was pressed, or a `MouseEvent`, indicating an exit button was pressed indicating the user did not want to save their choices.

In addition to letting the Android app know when the Preference Center experience is closed, it will also pass back the current consent state, which can be used throughout the application in determining what information can be collected.

To communicate back to the Android application, the `script` below makes use of `Javascript Interfaces`. 

_note: If you name your interface something other than `androidListener`, or `consentListener` within your Android app, ensure update the calls below, otherwise messages sent from this script will not be received in your Android app._

```html
<html>
    <head>
        ...
        <script>
            // Show the preference center experience
            window.semaphore.push(['showPreferences'])

            // Custom plugin to listen for the closing of the preference center experience
            var custom_plugin = {
                'init': function(host) {
                    console.log('plugin initialized');
                    androidListener.receiveMessage('custom plugin initialized');
                },
                'experienceHidden': function(host, config, reason) {
                    if ((reason.constructor.name === 'String' && reason === 'setConsent') || reason.constructor.name === 'MouseEvent') {
                        androidListener.receiveMessage('PreferenceCenterClosed');
                    }
                },
                'consentChanged': function(host, config, consent) {
                    consentListener.receiveMessage(JSON.stringify(consent));
                }
            }

            // Register the custom plugin with the Ketch Smart Tag
            window.semaphore = window.semaphore || [];
            window.semaphore.push(['registerPlugin', custom_plugin]);
        </script>
        ...
    </head>
</html>
```

## Showing the HTML page in an Android app

With the `index.html` page created, it's time to show it within an Android application.

To show our page, we'll utilize the Android `WebView`.

In your application in Android Studio,

- Create a new Empty Activity and name it `KetchPrefCenter`. It will serve as a reusable activity to collect and retrieve a users consent preferences for the purposes defined with in the Ketch Platform.

- In the new activity's layout file, add a `WebView` widget to the page ensuring it takes up the entire page and is constrained to the sides of the page.

- Next, override the `onCreate` function in the activities Kotlin file.
```kotlin
class KetchPrefCenter: AppCompatActivity() {
    override fun onCreate(savedInstance: Bundle?) {
        setContentView(R.layout.activity_ketch_pref_center)
        supportActionBar?.hide()

    }
}
```

- Inside the `onCreate` function, get an instance of the `WebView` widget added earlier and enable JavaScript execution
```kotlin
class KetchPrefCenter: AppCompatActivity() {
    override fun onCreate(savedInstance: Bundle?) {

        ...

        val myWebView: WebView = findViewById(R.id.webView)
        val webSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
    }
}
```

- Next, create a new class that inherits from `WebViewClientCompat()` to intercept web requests and load the HTML page created earlier. It will take in a `WebViewAssetLoader`, attempt to resolve the requested url to an application resource or asset. 
```kotlin
...

private class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClientCompat() {
    @RequiresApi(21)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }
}

```

- Back in the `onCreate` function, create a web view asset loader and an instance of the class above passing it the web view asset loader just created.
```kotlin
class KetchPrefCenter: AppCompatActivity() {
    override fun onCreate(savedInstance: Bundle?) {

        ...

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(this))
            .build()
        myWebView.webViewClient = LocalContentWebViewClient(assetLoader)
    }
}
```

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
            intent.putExtra("orgCode", "thatconf22_demo")
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