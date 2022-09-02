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
    @State private var ids = [Identity(code: "visitorId", value: "user@test.com")]
    
    var body: some View {
        Text("Hello world!")
            .padding()
        Button("Show Preference Center") {
            showingPopover = true
        }
        .popover(isPresented: $showingPopover) {
            PreferenceView(isPresented: $showingPopover, identities: $ids)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}


