# Ketch Smart Tag for the iOS app

This documentation demonstrates the Ketch Smart Tag usage for the Swift based iOS application.

## Prerequisites
- Registered [Ketch organization account](https://app.ketch.com/settings/organization) 
- Configured [application property](https://app.ketch.com/deployment/applications) record
- [Custom identity space](https://docs.ketch.com/hc/en-us/articles/360063594173-Managing-Properties#configuring-data-layer-setup-0-9)
- [index.html](./app/src/main/assets/index.html) Ketch Smart Tag integration bridge

## Quick Start

To integrate the Ketch Smart Tag into your Swift based iOS project follow these steps:


### Step 1. Copy the integration bridge into an app  

Add [index.html](./index.html) file with privacy web form wrapper to your project.

The `index.html` file makes use of WebKit `WKWebView` and `JavascriptInterface` to 
communicate back and forth with the native runtime of the iOS application.


### Step 2. Add Info.plist privacy trecking request.
Define `Info.plist` string for tracking allowance request with key `Privacy - Tracking Usage Description` (`NSUserTrackingUsageDescription`) that describes wanted purpose, e.g. "Please indicate whether you consent to our collection and use of your data in order to perform the operation(s) you’ve requested."


### Step 3. Create the Ketch Preferences Center view with the webView.

Integrate source code of preference settings view controller to your project.
File for UIKit integration: [ConsentViewController](./iOS%20Ketch%20Pref%20Center%20using%20Storyboard/ConsentViewController.swift)

Or view for SwiftUI integration: [ConsentView](./iOS%20Ketch%20Pref%20Center%20using%20SwiftUI/ConsentView.swift)

And add source code for config: [ConsentConfig](./ConsentConfig.swift)
    
    
### Step 4. Integrate calls for presentation of preference settings view (or view controller).

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

...

ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
    if case .authorized = authorizationStatus {
        let advertisingId = ASIdentifierManager.shared().advertisingIdentifier
        
        ...
    }
}
```

- Present Consent View, don't forget to run it in main thread:
```swift
import AppTrackingTransparency

...

ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
    if case .authorized = authorizationStatus {
        let advertisingId = ASIdentifierManager.shared().advertisingIdentifier
        
        DispatchQueue.main.async { [weak self] in
            self?.showConsent(advertisingId: advertisingId)
        }
    }
}

...

private func showConsent(advertisingId: UUID) {
    let vc = ConsentViewController(config:  ... )
    let navVC = UINavigationController(rootViewController: vc)
    present(navVC, animated: true)
}
```

- To keep the activity as configurable as the Ketch Smart Tag on the HTML page, it expects an organization code and property code to be passed in to it.
Configure `ConsentConfig` with those parameters:
```swift
ConsentConfig(
    propertyName: "#{your_property}#",
    orgCode: "#{your_org_code}#",
    advertisingIdentifier: advertisingId
)
```

- Full integration code with config:
```swift
import AppTrackingTransparency

...

ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
    if case .authorized = authorizationStatus {
        let advertisingId = ASIdentifierManager.shared().advertisingIdentifier
        
        DispatchQueue.main.async { [weak self] in
            self?.showConsent(advertisingId: advertisingId)
        }
    }
}

...

private func showConsent(advertisingId: UUID) {
    let vc = ConsentViewController(
        config: ConsentConfig(
            propertyName: "#{your_property}#",
            orgCode: "#{your_org_code}#",
            advertisingIdentifier: advertisingId
        )
    )
    let navVC = UINavigationController(rootViewController: vc)
    present(navVC, animated: true)
}
```

Example for UIKit integration: [ViewController](./iOS%20Ketch%20Pref%20Center%20using%20Storyboard/iOS%20Ketch%20Pref%20Center%20using%20Storyboard/ViewController.swift)

Example for SwiftUI integration: [ContentView](./iOS%20Ketch%20Pref%20Center%20using%20SwiftUI/iOS%20Ketch%20Pref%20Center%20using%20SwiftUI/ContentView.swift)


    


