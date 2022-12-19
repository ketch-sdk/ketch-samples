//
//  ContentView.swift
//  KetchSample
//

import SwiftUI
import KetchSDK
import AdSupport
import AppTrackingTransparency

class ContentViewModel: ObservableObject {
    @Published var authorizationDenied = false
    @Published var ketch: Ketch?
    @Published var ketchUI: KetchUI?

    init() { }

    func requestTrackingAuthorization() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            //  Delay after SwiftUI view appearing is required for alert presenting, otherwise it will not be shown
            ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
                if case .authorized = authorizationStatus {
                    let advertisingId = ASIdentifierManager.shared().advertisingIdentifier

                    self.setupKetch(advertisingIdentifier: advertisingId)
                } else if case .denied = authorizationStatus {
                    self.authorizationDenied = true
                }
            }
        }
    }

    private func setupKetch(advertisingIdentifier: UUID) {
        let ketch = KetchSDK.create(
            organizationCode: <#Your Organization Code#>,
            propertyCode: <#Your Property ID#>,
            environmentCode: <#Your Environment ID#>,
            controllerCode: <#Your Controller Code#>,
            identities: [.idfa(advertisingIdentifier.uuidString)]
        )

        ketch.add(plugins: [TCF(), CCPA()])

        self.ketch = ketch
        ketchUI = KetchUI(ketch: ketch)
    }
}

struct ContentView: View {
    @StateObject private var viewModel = ContentViewModel()
    @State var showDialogsAutomatically = false

    var body: some View {
        VStack(spacing: 40) {
            HStack {
                Text("Show Dialogs Automatically")
                Toggle("", isOn: $showDialogsAutomatically)
                    .labelsHidden()
            }

            if let ketch = viewModel.ketch {
                KetchTestView(ketch: ketch)
            }

            if let ketchUI = viewModel.ketchUI {
                KetchUITestView(ketchUI: ketchUI)
            }
        }
        .onAppear {
            viewModel.requestTrackingAuthorization()
        }
        .onChange(of: showDialogsAutomatically) { value in
            viewModel.ketchUI?.showDialogsIfNeeded = value
        }
        .alert(isPresented: $viewModel.authorizationDenied) {
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

struct KetchUITestView: View {
    @StateObject var ketchUI: KetchUI

    var body: some View {
        VStack(spacing: 40) {
            if ketchUI.consentStatus != nil {
                Button("Show Banner")     { ketchUI.showBanner() }
                Button("Show Modal")      { ketchUI.showModal() }
                Button("Show JIT")        { ketchUI.showJIT() }
                Button("Show Preference") { ketchUI.showPreference() }
            }
        }
        .fullScreenCover(item: $ketchUI.presentationItem, content: \.content)
    }
}

struct KetchTestView: View {
    enum Jurisdiction {
        static let GDPR = "gdpr"
        static let CCPA = "ccpa"
    }

    @StateObject var ketch: Ketch

    var body: some View {
        VStack(spacing: 40) {
            Button("Configuration")      { ketch.loadConfiguration() }
            Button("Configuration GDPR") { ketch.loadConfiguration(jurisdiction: Jurisdiction.GDPR) }
            Button("Configuration CCPA") { ketch.loadConfiguration(jurisdiction: Jurisdiction.CCPA) }

            if ketch.configuration != nil {
                Button("Invoke Rights")  { ketch.invokeRights(user: user) }
                Button("Get Consent")    { ketch.loadConsent() }
                Button("Update Consent") { ketch.updateConsent() }
            }
        }
    }

    private var user: KetchSDK.InvokeRightConfig.User {
        .init(
            email: "user@email.com",
            first: "FirstName",
            last: "LastName",
            country: nil,
            stateRegion: nil,
            description: nil,
            phone: nil,
            postalCode: nil,
            addressLine1: nil,
            addressLine2: nil
        )
    }
}
