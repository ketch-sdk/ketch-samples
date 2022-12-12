# ketch
Mobile SDK for Android

SDK includes core, ccpa, tcf and core-ui .aar library 
core - Base SDK library. It includes all necessary request to work with our backend
ccpa and tcf - Specific protocol plugins. 
core-ui - UI Module. It includes all predefined dialogs.

## Install SDK

### Maven/Gradle dependency
1. Open `build.gradle` in your root project
2. Add new repository:
```groovy
allprojects {
    repositories {
        ...
        maven {
            url "https://ketch.jfrog.io/artifactory/"
            credentials {
                username = <repo_username>
                password = <repo_password>
            }
        }
        ...
    }
}
```
3. Open module's `build.gradle` file
4. Update dependencies:
```groovy
dependencies {
    ...
    implementation 'com.ketch.android:core:<version>'
    implementation 'com.ketch.android:ccpa:<version>'
    implementation 'com.ketch.android:tcf:<version>'
    implementation 'com.ketch.android:core-ui:<version>'
    ...
    implementation "com.squareup.retrofit2:retrofit:<version>"
    implementation "com.squareup.retrofit2:converter-gson:<version>"
    implementation "com.squareup.okhttp3:okhttp:<version>"

    implementation "io.noties.markwon:core:<version>"
    implementation "io.noties.markwon:linkify:<version>"
    ...
}
```
5. Sync gradle

### Manual

The manual install is still possible, you need to perform next steps:
1. Just put SDK AAR file into` <project>/<module>/libs`
2. Open module's `build.gradle` file
3. Update dependencies:
```groovy
dependencies {
    ...
    implementation fileTree(dir: 'libs', include: ['*.aar'])
    ...
}
```
4. (optional) Sync gradle

## Setup

In order to use Ketch resources, `Ketch` should be properly initialized and setup. `KetchSdk` class should be used for that purpose
```kotlin
...    
    lateinit var ketch: Ketch

    override fun onCreate() {
        super.onCreate()
    
        ketch = KetchSdk.create(
            organization = <organization code>,
            property = <property>,
            environment = <environment>,
            controller = <controller>,
            identities = mapOf...
        )
        ...
```

Necessary `Plugins` can be added to `Ketch`
```kotlin
    ...
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
        ...
```

Also you can create create your own plugin using `com.ketch.android.plugin.Plugin`
```kotlin
class CustomPlugin(listener: (encodedString: String?, applied: Boolean) -> Unit) : Plugin(listener) {
    
    override fun isApplied(): Boolean = 
        configuration?.regulations?.contains(REGULATION) == true

    override fun consentChanged(consent: Consent) {
        ...
        listener.invoke(encodedString, applied)
    }

    override fun hashCode(): Int {
        return REGULATION.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return REGULATION.equals(other)
    }

    companion object {
        private const val REGULATION = "<regulation code>"
    }
}

```

## Requests

The next methods send requests to the back-end
- `ketch.loading` state flow will be `true` until all requests are loading
- On the finish you will get `KetchNotificationSuccess` or `KetchNotificationError` from `ketch.notification` channel 

### Get FullConfiguration for default parameters
Retrieves full organization configuration data.
- Call ketch.loadConfiguration() method.
- The result will be set into `configuration` StateFlow
```kotlin
ketch.loadConfiguration()
```

### Get FullConfiguration for specific jurisdiction
Retrieves full organization configuration data for specific jurisdiction.
- Call ketch.loadConfiguration(jurisdiction: String). Parameter `jurisdiction` : jurisdiction code
- The result will be set into `configuration` StateFlow
```kotlin
ketch.loadConfiguration(jurisdiction)
```

### Get Consent
Retrieves currently set consent status.
- Call ketch.loadConsent()
- The result will be set into `consent` StateFlow
```kotlin
ketch.loadConsent()
```

### Set Consent
Sends a request for updating consent status.
- Call ketch.updateConsent(purposes: Map<String, PurposeAllowedLegalBasis>, vendors: List<String>?)
- Parameter `purposes` : map of purpose code and PurposeAllowedLegalBasis
- Parameter `vendors` : list of vendors
- The updated consent will be set into `consent` StateFlow
```kotlin
val purposes = mapOf<String, PurposeLegalBasis>(
    ...
)

val vendors = listOf<String>(
    ...    
)

ketch.updateConsent(purposes, vendors)
```

### Invoke Rights
Invokes the specified rights.
- Parameter `right` : right code
- Parameter `user` : current user object

```kotlin
val user = User(
    email = <user email>,
    first = <first name>,
    last = <last name>,
    country = <country>,
    stateRegion = <state>,
    description = <description>,
    phone = <phone>,
    postalCode = <postal code>,
    addressLine1 = <address line 1>,
    addressLine2 = <address line 2>,        
)

ketch.invokeRights(
    right = <right code>,
    user = user,
)
```

## Core UI
### Setup
```kotlin
val ketchUi = KetchUi(this, ketch)
```

### Show Popup
Core UI contains implementations of `Banner`, `Modal`, `Just in Time` and `Preferences` Dialogs.
- To show `Banner` you have to invoke `ketchUi.showBanner(configuration, consent)`
- To show `Modal` you have to invoke `ketchUi.showModal(configuration, consent)`
- To show `Just in Time` you have to invoke `ketchUi.showJit(configuration, consent)`
- To show `Preferences` you have to invoke `ketchUi.showPreference(configuration, consent)`

### Show Popup Automatically
Core UI observes `config` and `consent` StateFlow in `Ketch`. 
To show `dialogs` automatically you should set `true` to `showDialogsIfNeeded` in `ketchUi` and 
successively invoke `ketch.loadConfiguration()` and `ketch.loadConsent()` methods in `ketch`
```kotlin
ketchUi.showDialogsIfNeeded = true
```