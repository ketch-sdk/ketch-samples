//
//  ConsentView.swift
//  iOS Ketch Pref Center using SwiftUI
//

import SwiftUI
import WebKit

struct ConsentView: UIViewRepresentable {
    let config: ConsentConfig
    @Environment(\.presentationMode) private var presentationMode

    func makeUIView(context: Context) -> some UIView {
        let preferences = WKWebpagePreferences()
        preferences.allowsContentJavaScript = true

        let configuration = WKWebViewConfiguration()
        configuration.defaultWebpagePreferences = preferences

        let consentHandler = ConsentHandler(userDefaults: config.userDefaults) {
            presentationMode.wrappedValue.dismiss()
        }

        configuration.userContentController.add(consentHandler, name: "onInit")
        configuration.userContentController.add(consentHandler, name: "onUpdate")
        configuration.userContentController.add(consentHandler, name: "onClose")

        let webView = WKWebView(frame: .zero, configuration: configuration)
        
        if let fileUrl = config.fileUrl {
            webView.load(URLRequest(url: fileUrl))
        }

        return webView
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) { }
}
