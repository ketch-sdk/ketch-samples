//
//  ContentView.swift
//  iOS Pref Center Demo
//
//  Created by Ryan Overton on 8/24/22.
//

import SwiftUI
import UIKit
import WebKit

struct ContentView: View {
    @State private var showingPopover = false

    var body: some View {
        VStack(spacing: 10) {
            Text("Hello world!")
            Button("Show Preference Center") { showingPopover = true }
            .sheet(isPresented: $showingPopover) {
                ConsentView(
                    config: .init(
                        propertyName: "website_smart_tag",
                        orgCode: "transcenda",
                        identities: [ConsentView.Identity(code: "visitorId", value: "user@test.com")]
                    )
                )
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}


