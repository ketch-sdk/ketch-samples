//
//  ConsentView.swift
//  iOS Ketch Pref Center using SwiftUI
//

import SwiftUI
import WebKit

struct ConsentView: UIViewRepresentable {
    let config: Config
    @Environment(\.presentationMode) private var presentationMode

    func makeUIView(context: Context) -> some UIView {
        let preferences = WKWebpagePreferences()
        preferences.allowsContentJavaScript = true
        
        let configuration = WKWebViewConfiguration()
        configuration.defaultWebpagePreferences = preferences

        let consentHandler = ConsentHandler(userDefaults: config.userDefaults) {
            presentationMode.wrappedValue.dismiss()
        }

        configuration.userContentController.add(consentHandler, name: "iosListener")

        let webView = WKWebView(frame: .zero, configuration: configuration)
        if let fileUrl = config.fileUrl {
            webView.load(URLRequest(url: fileUrl))
        }

        return webView
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) { }
}

extension ConsentView {
    struct Config {
        let propertyName: String
        let orgCode: String
        let identities: [Identity]
        let htmlFileName: String
        let userDefaults: UserDefaults

        init(
            propertyName: String,
            orgCode: String,
            identities: [Identity],
            htmlFileName: String = "index",
            userDefaults: UserDefaults = .standard
        ) {
            self.propertyName = propertyName
            self.orgCode = orgCode
            self.identities = identities
            self.htmlFileName = htmlFileName
            self.userDefaults = userDefaults
        }

        var fileUrl: URL? {
            let url = Bundle.main.url(forResource: htmlFileName, withExtension: "html")!

            var urlComponents = URLComponents(string: url.absoluteString)
            urlComponents?.queryItems = queryItems

            return urlComponents?.url
        }

        var queryItems: [URLQueryItem] {
            [URLQueryItem(name: "propertyName", value: propertyName),
             URLQueryItem(name: "orgCode", value: orgCode)]
            + identities.map { URLQueryItem(name: $0.code, value: $0.value) }
        }
    }

    enum Identity {
        case advertisingIdentifier(UUID)
        case custom(code: String, value: String)

        var code: String {
            switch self {
            case .advertisingIdentifier: return "ios_advertising_id"
            case .custom(let code, _): return code
            }
        }

        var value: String {
            switch self {
            case .advertisingIdentifier(let value): return value.uuidString
            case .custom(_, let value): return value
            }
        }
    }
}

private class ConsentHandler: NSObject, WKScriptMessageHandler {
    var onClose: () -> Void
    private let userDefaults: UserDefaults
    private var consent: [String: Any]?

    init(userDefaults: UserDefaults, onClose: @escaping () -> Void) {
        self.onClose = onClose
        self.userDefaults = userDefaults
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        print(message.body)
        if let payload = message.body as? [String: Any],
           let event = payload["event"] as? String {
            switch event {
            case "PreferenceCenterSubmit":
                handleEvent(consent: consent ?? [:])
                onClose()

            case "PreferenceCenterExit":
                onClose()

            case "consentChanged":
                consent = payload["consent"] as? [String : Any]

            default: break
            }
        }
    }

    private func handleEvent(consent: [String: Any]) {
        let vendors = consent["vendors"] as? [String]
        let purposes = consent["purposes"] as? [String: Any]

        purposes?.forEach { (key, value) in
            if let userDefaultsKey = TCF_Key.userDefaultsKey(forValue: key) {
                userDefaults.set(value, forKey: userDefaultsKey)
            }
        }
    }
}

enum TCF_Key {
    static private let keys = [
        "analytics": "analytics_TCF_KEY",
        "behavioral_advertising": "behavioral_advertising_TCF_KEY",
        "email_marketing": "email_marketing_TCF_KEY",
        "essential_services": "essential_services_TCF_KEY",
        "somepurpose_key": "somepurpose_key_TCF_KEY"
    ]

    static func userDefaultsKey(forValue value: String) -> String? {
        keys[value]
    }
}

struct PreferenceView_Previews: PreviewProvider {
    static var previews: some View {
        ConsentView(
            config: .init(
                propertyName: "website_smart_tag",
                orgCode: "transcenda",
                identities: []
            )
        )
    }
}
