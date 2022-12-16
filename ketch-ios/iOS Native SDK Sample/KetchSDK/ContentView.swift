//
//  ContentView.swift
//  KetchSample
//

import SwiftUI
import KetchSDK

struct ContentView: View {
    let ketch: Ketch
    @ObservedObject var ketchUI: KetchUI

    init() {
        ketch = KetchSDK.create(
            organizationCode: <#Your Organization Code#>,
            propertyCode: <#Your Property ID#>,
            environmentCode: <#Your Environment ID#>,
            controllerCode: <#Your Controller Code#>,
            identities: ["idfa" : <#Your Advertisement ID#>]
        )

        ketch.add(plugins: [TCF(), CCPA()])
        ketchUI = KetchUI(ketch: ketch)
    }

    var body: some View {
        ZStack {
            VStack(spacing: 40) {
                Button("Configuration")         { ketch.loadConfiguration() }
                Button("Configuration GDPR")    { ketch.loadConfiguration(jurisdiction: Jurisdiction.GDPR) }
                Button("Configuration CCPA")    { ketch.loadConfiguration(jurisdiction: Jurisdiction.CCPA) }
                Button("Invoke Rights")         { ketch.invokeRights(user: user) }
                Button("Get Consent")           { ketch.loadConsent() }
                Button("Update Consent")        { ketch.updateConsent() }
                    .padding(.bottom, 40)

                Button("Show Banner")           { ketchUI.showBanner() }
                Button("Show Modal")            { ketchUI.showModal() }
                Button("Show JIT")              { ketchUI.showJIT() }
                Button("Show Preference")       { ketchUI.showPreference() }
            }
        }
        .fullScreenCover(item: $ketchUI.presentationItem, content: \.content)
    }
}

extension ContentView {
    var user: KetchSDK.InvokeRightConfig.User {
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

    enum Jurisdiction {
        static let GDPR = "gdpr"
        static let CCPA = "ccpa"
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
