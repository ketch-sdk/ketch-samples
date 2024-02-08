# Sample App for Ketch Mobile SDK 

## Prerequisites

- Install and run [XCode](https://apps.apple.com/us/app/xcode/id497799835?mt=12) from the IOS App Store
    - When running for the first time, make sure to check the box for "iOS Simulator" so that you also get a mobile emulator to test on.

## Quick Guide

#### Step 1. Clone the repository and install dependencies

```
git clone git@github.com:ketch-sdk/ketch-samples.git
cd "ketch-ios/iOS Ketch SDK SwiftUI"
```

#### Step 2. Run the app in XCode

Open the project workspace `KetchSDK.xcworkspace` in the XCode.

Click Run to build and run the app on the simulator or a device.


Optionally you can add your organization code and property name to
`ketch-ios/iOS Ketch SDK SwiftUI/KetchSDK/ContentView.swift`:

```swift
organizationCode: "???????????????????",
propertyCode: "???????????????????",
```


## Updating the Sample App to the latest version of SDK

Package Dependancies => KetchSDK => [drop-down menu] => Update Package
 
![update-package.png](docs%2Fupdate-package.png)
