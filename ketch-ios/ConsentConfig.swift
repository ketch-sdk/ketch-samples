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
        switch message.name {
        case "onInit", "onUpdate": break
        case "onClose":
            if let payload = message.body as? String {
                handleEvent(with: payload)
            }
            onClose()

        default: break
        }
    }

    private func handleEvent(with payload: String) {
        guard let payloadData = payload.data(using: .utf8),
              let consent = try? JSONDecoder().decode(ConsentModel.self, from: payloadData)
        else { return }

        let keyUSPrivacy = ConsentModel.CodingKeys.valueUSPrivacy.rawValue
        if consent.valueUSPrivacy?.isEmpty == false {
            userDefaults.set(consent.valueUSPrivacy, forKey: keyUSPrivacy)
        } else {
            userDefaults.removeObject(forKey: keyUSPrivacy)
        }

        let keyTC = ConsentModel.CodingKeys.valueTC.rawValue
        if consent.valueTC?.isEmpty == false {
            userDefaults.set(consent.valueTC, forKey: keyTC)
        } else {
            userDefaults.removeObject(forKey: keyTC)
        }

        let keyGDPRApplies = ConsentModel.CodingKeys.valueGDPRApplies.rawValue
        if let valueGDPRApplies = consent.valueGDPRApplies {
            userDefaults.set(valueGDPRApplies, forKey: keyGDPRApplies)
        } else {
            userDefaults.removeObject(forKey: keyGDPRApplies)
        }
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
