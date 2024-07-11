//
//  ContentView.swift
//  iOS Pref Center Demo
//

import SwiftUI
import AdSupport
import AppTrackingTransparency

private var config: ConsentConfig?

struct ContentView: View {
    @State private var showAuthorizationDenied = false
    @State private var configItem: ConsentConfig?

    var body: some View {
        VStack {
            Button("Show Preference Center") {
                configItem = config
            }
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                //  Delay after SwiftUI view appearing is required for alert presenting, otherwise it will not be shown
                ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
                    if case .authorized = authorizationStatus {
                        let advertisingId = ASIdentifierManager.shared().advertisingIdentifier

                        config = ConsentConfig.configure(
                            orgCode: "ketch_samples",
                            propertyName: "ios",
                            advertisingIdentifier: advertisingId
                        )
                    } else if case .denied = authorizationStatus {
                        showAuthorizationDenied = true
                    }
                }
            }
        }
        .sheet(item: $configItem) { configItem in
            ConsentView(config: configItem)
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
