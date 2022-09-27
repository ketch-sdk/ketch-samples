//
//  ContentView.swift
//  iOS Pref Center Demo
//

import SwiftUI
import AdSupport
import AppTrackingTransparency

private var advertisingId: UUID?

struct ContentView: View {
    @State private var showingPopover = false
    @State private var showAuthorizationDenied = false

    var body: some View {
        VStack {
            Button("Show Preference Center") {
                ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
                    if case .authorized = authorizationStatus {
                        advertisingId = ASIdentifierManager.shared().advertisingIdentifier
                        showingPopover = true
                    } else if case .denied = authorizationStatus {
                        showAuthorizationDenied = true
                    }
                }
            }
        }
        .sheet(isPresented: $showingPopover) {
            ConsentView(
                config: ConsentConfig(
                    orgCode: "<#Your Organization Code#>",
                    propertyName: "<#Your Property ID#>",
                    advertisingIdentifier: advertisingId!
                )
            )
        }
        .alert(isPresented: $showAuthorizationDenied) {
            Alert(
                title: Text("Tracking Authorization Denied by app settings"),
                message: Text("Please allow tracking in Settings -> Privacy -> Tracking"),
                primaryButton: .cancel(Text("Cancel")),
                secondaryButton: .default(
                    Text("Edit preferences"),
                    action: {
                        if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(settingsURL)
                        }
                    }
                )
            )
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
