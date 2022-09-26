# Ketch iOS Samples

- [Using the Ketch Smart Tag within a storyboard based Swift project](./iOS%20Ketch%20Pref%20Center%20using%20Storyboard/)
- [Using the Ketch Smart Tag within a SwiftUI based Swift project](./iOS%20Ketch%20Pref%20Center%20using%20SwiftUI/)

For integration of Ketch Smart Tag to your project its needed to perform following steps:

1. Define Info.plist string for tracking allowance request with key "Privacy - Tracking Usage Description" (NSUserTrackingUsageDescription) that describes wanted purpose, e.g. "Please indicate whether you consent to our collection and use of your data in order to perform the operation(s) you’ve requested."

2. Add html file with privacy web form wrapper to your project. Page located at: /ketch-samples/ketch-ios/index.html

3. Integrate source code of preference settings view controller to your project.
    File for UIKit integration:
    /ketch-samples/ketch-ios/iOS Ketch Pref Center using Storyboard/ConsentViewController.swift
    
    Or view for SwiftUI integration:
    ketch-samples/ketch-ios/iOS Ketch Pref Center using SwiftUI/ConsentView.swift
    
    And add source code for config:
    ketch-samples/ketch-ios/ConsentConfig.swift

 4. Integrate calls for presentation of preference settings view (or view controller):
    Example for UIKit integration:
    ketch-samples/ketch-ios/iOS Ketch Pref Center using Storyboard/iOS Ketch Pref Center using Storyboard/ViewController.swift
    
    Example for SwiftUI integration:
    ketch-samples/ketch-ios/iOS Ketch Pref Center using SwiftUI/iOS Ketch Pref Center using SwiftUI/ContentView.swift
    


