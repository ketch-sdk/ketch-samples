//
//  ConsentConfig.swift
//  iOS Ketch Pref Center using SwiftUI
//

import Foundation
import WebKit

struct ConsentConfig {
    let orgCode: String
    let propertyName: String
    let advertisingIdentifier: UUID
    let htmlFileName: String
    let userDefaults: UserDefaults
    private var configWebApp: WKWebView?

    init(
        orgCode: String,
        propertyName: String,
        advertisingIdentifier: UUID,
        htmlFileName: String = "index",
        userDefaults: UserDefaults = .standard
    ) {
        self.propertyName = propertyName
        self.orgCode = orgCode
        self.advertisingIdentifier = advertisingIdentifier
        self.htmlFileName = htmlFileName
        self.userDefaults = userDefaults

        configWebApp = webView
    }

    var fileUrl: URL? {
        let url = Bundle.main.url(forResource: htmlFileName, withExtension: "html")!

        var urlComponents = URLComponents(string: url.absoluteString)
        urlComponents?.queryItems = queryItems

        return urlComponents?.url
    }

    var queryItems: [URLQueryItem] {
        let base64EncodedString = try? JSONSerialization
            .data(withJSONObject: ["idfa": advertisingIdentifier.uuidString])
            .base64EncodedString()

        return [
            URLQueryItem(name: "propertyName", value: propertyName),
            URLQueryItem(name: "orgCode", value: orgCode),
            URLQueryItem(name: "encodedIdentities", value: base64EncodedString)
        ]
    }

    private var webView: WKWebView {
        let preferences = WKWebpagePreferences()
        preferences.allowsContentJavaScript = true

        let configuration = WKWebViewConfiguration()
        configuration.defaultWebpagePreferences = preferences

        let consentHandler = ConsentHandler(userDefaults: userDefaults) { }

        ConsentHandler.Event.allCases.forEach { event in
            configuration.userContentController.add(consentHandler, name: event.rawValue)
            print(event)
        }

        let webView = WKWebView(frame: .zero, configuration: configuration)

        if let fileUrl = fileUrl {
            webView.load(URLRequest(url: fileUrl))
        }

        return webView
    }
}

class ConsentHandler: NSObject, WKScriptMessageHandler {
    var onClose: () -> Void
    private let userDefaults: UserDefaults
    private var consent: [String: Any]?

    init(userDefaults: UserDefaults, onClose: @escaping () -> Void) {
        self.onClose = onClose
        self.userDefaults = userDefaults
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard let event = Event(rawValue: message.name) else {
            print("Ketch Preference Center: Unable to handle unknown event \"\(message.name)\"")
            return
        }

        switch event {
        case .onInit, .save, .notShow: break
        case .close:
            onClose()

        case .updateCCPA:
            if let value = message.body as? String {
                save(value: value, for: .valueUSPrivacy)
            }

        case .updateTCF:
            if let value = message.body as? String {
                save(value: value, for: .valueTC)
//                save(value: consent.valueGDPRApplies, for: .valueGDPRApplies)
            }
        }
    }

    private func handleEvent(with payload: String) {
        guard let payloadData = payload.data(using: .utf8),
              let consent = try? JSONDecoder().decode(ConsentModel.self, from: payloadData)
        else { return }

        save(value: consent.valueUSPrivacy, for: .valueUSPrivacy)
        save(value: consent.valueTC, for: .valueTC)
        save(value: consent.valueGDPRApplies, for: .valueGDPRApplies)
    }

    private func save(value: String?, for key: ConsentModel.CodingKeys) {
        let keyValue = key.rawValue
        if value?.isEmpty == false {
            userDefaults.set(value, forKey: keyValue)
        } else {
            userDefaults.removeObject(forKey: keyValue)
        }
    }

    private func save(value: Int?, for key: ConsentModel.CodingKeys) {
        let keyValue = key.rawValue
        if let value = value {
            userDefaults.set(value, forKey: keyValue)
        } else {
            userDefaults.removeObject(forKey: keyValue)
        }
    }
}

extension ConsentHandler {
    enum Event: String, CaseIterable {
        case onInit = "onInit"
        case save = "onSave"
        case close = "onClose"
        case notShow = "onNotShow"
        case updateCCPA = "onCCPAUpdate"
        case updateTCF = "onTCFUpdate"
    }
}

private struct ConsentModel: Codable {
    let valueUSPrivacy: String?
    let valueTC: String?
    let valueGDPRApplies: Int?

    enum CodingKeys: String, CodingKey {
        case valueUSPrivacy = "IABUSPrivacy_String"
        case valueTC = "IABTCF_TCString"
        case valueGDPRApplies = "IABTCF_gdprApplies"
    }
}
