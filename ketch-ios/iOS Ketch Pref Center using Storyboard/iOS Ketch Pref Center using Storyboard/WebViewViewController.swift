//
//  WebViewViewController.swift
//  iOS Ketch Pref Center using Storyboard
//
//  Created by Ryan Overton on 8/26/22.
//

import UIKit
import WebKit

class WebViewViewController: UIViewController {

    private let url: URL
    
    init (url: URL, title: String) {
        self.url = url
        super.init(nibName: nil, bundle: nil)
        self.title = title
    }
    
    required init?(coder: NSCoder) {
        fatalError()
    }
    
    func getWebViewConfig() -> WKWebViewConfiguration {
        let preferences = WKWebpagePreferences()
        preferences.allowsContentJavaScript = true
        
        let configuration = WKWebViewConfiguration()
        configuration.defaultWebpagePreferences = preferences
        
        let userContentController = WKUserContentController()
        userContentController.add(self, name: "onInit")
        userContentController.add(self, name: "onUpdate")
        userContentController.add(self, name: "onClose")
        
        configuration.userContentController = userContentController
        
        return configuration
    }
    
    func configureWebViewLayout(webView: inout WKWebView) {
        let layoutGuide = view.safeAreaLayoutGuide

        webView.translatesAutoresizingMaskIntoConstraints = false
        webView.leadingAnchor.constraint(equalTo: layoutGuide.leadingAnchor).isActive = true
        webView.trailingAnchor.constraint(equalTo: layoutGuide.trailingAnchor).isActive = true
        webView.topAnchor.constraint(equalTo: layoutGuide.topAnchor).isActive = true
        webView.bottomAnchor.constraint(equalTo: layoutGuide.bottomAnchor).isActive = true
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        
        // Get WKWEbView configuration
        let configuration = getWebViewConfig()
        
        // Create the WebView
        var webView = WKWebView(frame: .zero, configuration: configuration)
        
        // Add the WebView to the current view
        view.addSubview(webView)
        
        // Configure WebViews Layout
        configureWebViewLayout(webView: &webView)
        
        // Navigate to URL in the WebView
        webView.load(URLRequest(url: url))
    }
}

class MessageJSONModel: Codable {
    let IABUSPrivacy_String: String
    let IABTCF_TCString: String
    let IABTCF_gdprApplies: Int?
    
    enum CodingKeys: String, CodingKey {
        case IABUSPrivacy_String
        case IABTCF_TCString
        case IABTCF_gdprApplies
    }
}

extension WebViewViewController: WKScriptMessageHandler {
    // Capture postMessage() calls inside loaded JavaScript from the webpage. Note that a Boolean
    // will be parsed as a 0 for false and 1 for true in the message's body. See WebKit documentation:
    // https://developer.apple.com/documentation/webkit/wkscriptmessage/1417901-body.
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if message.name == "onInit" {
            let defaults = UserDefaults.standard
            let IABUSPrivacy_String = defaults.string(forKey: "IABUSPrivacy_String")
            let IABTCF_TCString = defaults.string(forKey: "IABTCF_TCString")
            let IABTCF_gdprApplies = defaults.integer(forKey: "IABTCF_gdprApplies")
            print(IABUSPrivacy_String)
            print(IABTCF_TCString)
            print(IABTCF_gdprApplies)
            print(message.name)
        }
        if message.name == "onUpdate", let messageBody = message.body as? String {
            print(message.name)
            print(messageBody)
        }
        if message.name == "onClose", let messageBody = message.body as? String {
            print(message.name)
            print(messageBody)
            let jsonData = messageBody.data(using: .utf8)

            let jsonDecode = try! JSONDecoder().decode(MessageJSONModel.self, from: jsonData!)
            let IABUSPrivacy_String = jsonDecode.IABUSPrivacy_String
            let IABTCF_TCString = jsonDecode.IABTCF_TCString
            let IABTCF_gdprApplies = jsonDecode.IABTCF_gdprApplies

            let defaults = UserDefaults.standard

            if (!IABUSPrivacy_String.isEmpty) {
                defaults.set(IABUSPrivacy_String, forKey: "IABUSPrivacy_String")
            } else {
                defaults.removeObject(forKey: "IABUSPrivacy_String")
            }
            
            if (!IABTCF_TCString.isEmpty) {
                defaults.set(IABTCF_TCString, forKey: "IABTCF_TCString")
            } else {
                defaults.removeObject(forKey: "IABTCF_TCString")
            }
            
            if (IABTCF_gdprApplies != nil) {
                defaults.set(IABTCF_gdprApplies, forKey: "IABTCF_gdprApplies")
            } else {
                defaults.removeObject(forKey: "IABTCF_gdprApplies")
            }
            
            dismiss(animated: true, completion: nil)
        }
    }
}


