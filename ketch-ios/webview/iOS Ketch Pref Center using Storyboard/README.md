# Ketch Smart Tag for the iOS app using Storyboard

This document demonstrates the Ketch Smart Tag usage for the Swift based native iOS application.

It handles the storage of the corresponding policy strings to SharedPreferences,
as per standards requirements for the in-app support:
- [IAB Europe Transparency & Consent Framework](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details)
- [CCPA Compliance Mechanism](https://github.com/InteractiveAdvertisingBureau/USPrivacy/blob/master/CCPA/USP%20API.md#in-app-support)

## Prerequisites
- Registered [Ketch organization account](https://app.ketch.com/settings/organization) 
- Configured [application property](https://app.ketch.com/deployment/applications) record
- [Custom identity space](https://docs.ketch.com/hc/en-us/articles/360063594173-Managing-Properties#configuring-data-layer-setup-0-9)
- [index.html](./index.html) Ketch Smart Tag integration bridge

## Quick Start

Before we start, take a look at the fully functional sample iOS app in current repo,
where the following steps implement the Ketch Smart Tag into your Storyboard based iOS project.

### Step 1. Copy the integration bridge into the app

Add [index.html](./../index.html) file with privacy web form wrapper to your project.

The `index.html` file makes use of WebKit `WKWebView` and `JavascriptInterface` to 
communicate back and forth with the native runtime of the iOS application.

### Step 2. Add Info.plist privacy tracking request

Define `Info.plist` string for tracking allowance request with key 
`Privacy - Tracking Usage Description` (`NSUserTrackingUsageDescription`) 
that describes wanted purpose, e.g. "Please indicate whether you consent to our collection and use 
of your data in order to perform the operation(s) you’ve requested."

### Step 3. Create the Ketch Preferences Center view with the webView

Integrate source code of preference settings view controller to your project.

File for UIKit integration: [ConsentViewController](./ConsentViewController.swift)

And add source code for config: [ConsentConfig](./../ConsentConfig.swift)
    
### Step 4. Integrate calls for presentation of preference settings view (or view controller)

- Request permission for application tracking using `requestTrackingAuthorization` from `AppTrackingTransparency.ATTrackingManager`:

```swift
import AppTrackingTransparency

...

ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
    if case .authorized = authorizationStatus {
        ...
    }
}
```

- Retrieve `advertisingIdentifier` from `AdSupport.ASIdentifierManager`:

```swift
import AppTrackingTransparency
import AdSupport

...
    
ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
    if case .authorized = authorizationStatus {
        let advertisingId = ASIdentifierManager.shared().advertisingIdentifier
        
        ...
    }
}
```

- Create ConsentConfig by .configure(:...) method and save it for future preferencesCenter launch. To keep the activity as configurable as the Ketch Smart Tag on the HTML page, it expects an organization code and property code to be passed in to it:

```swift
import AppTrackingTransparency
import AdSupport

...

class ViewController: UIViewController {
    var config: ConsentConfig?

...

override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    
    ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
        if case .authorized = authorizationStatus {
            let advertisingId = ASIdentifierManager.shared().advertisingIdentifier
            
            self?.config = ConsentConfig.configure(
                orgCode: "#{your_org_code}#",
                propertyName: "#{your_property}#",
                advertisingIdentifier: advertisingId
            )
        }
    }
```

- Show PreferenceCenter once you need to launch preferences setup:

```swift
let vc = ConsentViewController(config: config)
let navVC = UINavigationController(rootViewController: vc)
present(navVC, animated: true)
```

- Full integration code with config:

```swift
import AppTrackingTransparency
import AdSupport

...

class ViewController: UIViewController {
    var config: ConsentConfig?

...

override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    
    ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
        if case .authorized = authorizationStatus {
            let advertisingId = ASIdentifierManager.shared().advertisingIdentifier
            
            self?.config = ConsentConfig.configure(
                orgCode: "#{your_org_code}#",
                propertyName: "#{your_property}#",
                advertisingIdentifier: advertisingId
            )
        }
    }

...

func showPreferencesCenter() {
    guard let config = config else { return }

    let vc = ConsentViewController(config: config)
    let navVC = UINavigationController(rootViewController: vc)

    present(navVC, animated: true)
}
```
