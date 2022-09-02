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
        userContentController.add(self, name: "iosListener")
        
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

extension WebViewViewController: WKScriptMessageHandler {
    // Capture postMessage() calls inside loaded JavaScript from the webpage. Note that a Boolean
    // will be parsed as a 0 for false and 1 for true in the message's body. See WebKit documentation:
    // https://developer.apple.com/documentation/webkit/wkscriptmessage/1417901-body.
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        print("message:" + (message.body as! String))
        if (message.body as? String == "PreferenceCenterClosed") {
                dismiss(animated: true, completion: nil)
        }
    }
}


