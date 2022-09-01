//
//  PreferenceView.swift
//  iOS Ketch Pref Center using SwiftUI
//
//  Created by Ryan Overton on 8/31/22.
//

import SwiftUI
import WebKit

struct Identity {
    let code: String
    let value: String
}

struct WebView: UIViewRepresentable {
    @Binding var isPresented: Bool
    @Binding var identities: [Identity]
    let htmlFileName: String
    
    
    func makeUIView(context: Context) -> some UIView {
        let preferences = WKWebpagePreferences()
        preferences.allowsContentJavaScript = true
        
        let configuration = WKWebViewConfiguration()
        configuration.defaultWebpagePreferences = preferences
        let handler = MessageHandler(isPresented: $isPresented)
        configuration.userContentController.add(handler, name: "iosListener")
        let webView = WKWebView(frame: .zero, configuration: configuration)
        return webView
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) {
        let wv = uiView as? WKWebView
        var url = Bundle.main.url(forResource: htmlFileName, withExtension: "html")!
        for identity in identities {
            url = url.appending(identity.code, value: identity.value)
        }
        wv?.load(URLRequest(url: url))
    }
    
    class MessageHandler: NSObject, WKScriptMessageHandler {
        @Binding var isPresented: Bool
        init(isPresented: Binding<Bool>) {
            self._isPresented = isPresented
        }
        func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
            print(message.body)
            if (message.body as! String == "PreferenceCenterClosed") {
                isPresented = false;
            }
        }
    }
}

struct PreferenceView: View {
    @Binding var isPresented: Bool
    @Binding var identities: [Identity]
    var body: some View {
        WebView(isPresented: $isPresented, identities: $identities, htmlFileName: "index")
    }
}

struct PreferenceView_Previews: PreviewProvider {
    static var previews: some View {
        PreferenceView(isPresented: .constant(true), identities: .constant([]))
    }
}

extension URL {

    func appending(_ queryItem: String, value: String?) -> URL {

        guard var urlComponents = URLComponents(string: absoluteString) else { return absoluteURL }

        // Create array of existing query items
        var queryItems: [URLQueryItem] = urlComponents.queryItems ??  []

        // Create query item
        let queryItem = URLQueryItem(name: queryItem, value: value)

        // Append the new query item in the existing query items array
        queryItems.append(queryItem)

        // Append updated query items array in the url component object
        urlComponents.queryItems = queryItems

        // Returns the url from new url components
        return urlComponents.url!
    }
}
