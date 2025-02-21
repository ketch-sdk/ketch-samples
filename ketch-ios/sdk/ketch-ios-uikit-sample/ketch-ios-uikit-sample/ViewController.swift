//
//  ViewController.swift
//  ketch-ios-uikit-sample
//
//  Created by Roman Simenok on 19.02.2025.
//

import UIKit
import KetchSDK

class ViewController: UIViewController {
    private var ketchUI: KetchUI!
    
    @IBOutlet weak var organizationText: UITextField!
    @IBOutlet weak var propertyText: UITextField!
    @IBOutlet weak var environmentText: UITextField!
    @IBOutlet weak var languageText: UITextField!
    @IBOutlet weak var jurisdictionText: UITextField!
    @IBOutlet weak var regionText: UITextField!
    @IBOutlet weak var idKeyText: UITextField!
    @IBOutlet weak var idValueText: UITextField!
    @IBOutlet weak var resetButton: UIButton!
    @IBOutlet weak var addButton: UIButton!
    
    // API Region
    @IBOutlet weak var prodUS: UIButton!
    @IBOutlet weak var prodEU: UIButton!
    @IBOutlet weak var uat: UIButton!
    
    // Allowed Tabs
    @IBOutlet weak var overviewAllowed: UIButton!
    @IBOutlet weak var consentsAllowed: UIButton!
    @IBOutlet weak var subscriptionsAllowed: UIButton!
    @IBOutlet weak var rightsAllowed: UIButton!
    
    // Initial Tabs
    @IBOutlet weak var overviewInitial: UIButton!
    @IBOutlet weak var consentInitial: UIButton!
    @IBOutlet weak var subscriptionsInitial: UIButton!
    @IBOutlet weak var rightsInitial: UIButton!
    
    private var identities = [Ketch.Identity]()

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let ketch = KetchSDK.create(
            // Replace below with your Ketch organization code
            organizationCode: "ketch_samples",
            // Repalce below with your Ketch property code
            propertyCode: "ios",
            environmentCode: "production",
            identities: [
                // Replace below with your Ketch identifier name and value
                Ketch.Identity(key: "idfa", value: "00000000-0000-0000-0000-000000000000")
            ]
        )
        
        // Create the KetchUI object
        ketchUI = KetchUI(ketch: ketch)
        
        // Add our listener to the ketchUI class
        ketchUI.eventListener = self
    }
    
    // MARK: - Actions
    
    @IBAction func didTapReset(_ sender: UIButton) {
        identities.removeAll()
        resetButton.isEnabled = false
    }
    
    @IBAction func didTapAdd(_ sender: UIButton) {
        resetButton.isEnabled = true
        addButton.isEnabled = false
        identities.append(Ketch.Identity(key: idKeyText.text!, value: idValueText.text!))
        
        idKeyText.text = ""
        idValueText.text = ""
    }
    
    @IBAction func textFieldDidChange(_ sender: UITextField) {
        let active = !(idKeyText.text ?? "").isEmpty && !(idValueText.text ?? "").isEmpty
        addButton.isEnabled = active
    }
    
    @IBAction func didChangeApiRegion(_ sender: UIButton) {
        [prodUS, prodEU, uat].forEach { $0?.isSelected = false }
        
        sender.isSelected = true
    }
    
    @IBAction func didSelectAllowedTab(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
    }
    
    @IBAction func didChangeInitialTab(_ sender: UIButton) {
        [overviewInitial, consentInitial, subscriptionsInitial, rightsInitial].forEach { $0?.isSelected = false }
        
        sender.isSelected = true
    }
    
    @IBAction func showConsent(_ sender: UIButton) {
        var parameters = makeParameters
        parameters.append(.forceExperience(.consent))
        ketchUI.reload(with: parameters)
    }
    
    @IBAction func showPreferences(_ sender: UIButton) {
        var parameters = makeParameters
        
        let selectedTabs = selectedTabsNames
        let initialTab = initialTab
        if !selectedTabs.isEmpty {
            parameters.append(.preferencesTabs(selectedTabs.joined(separator: ",")))
            
            if selectedTabs.contains(initialTab),
               let prefTab = KetchUI.ExperienceOption.PreferencesTab(rawValue: initialTab) {
                parameters.append(.preferencesTab(prefTab))
            }
        }
        
        parameters.append(.forceExperience(.preferences))
        ketchUI.reload(with: parameters)
    }
    
    @IBAction func showPrivacyStrings(_ sender: UIButton) {
        let keys = ["IABTCF_CmpSdkID",
                    "IABTCF_CmpSdkVersion",
                    "IABTCF_PolicyVersion",
                    "IABTCF_gdprApplies",
                    "IABTCF_PublisherCC",
                    "IABTCF_PurposeOneTreatment",
                    "IABTCF_UseNonStandardTexts",
                    "IABTCF_TCString",
                    "IABTCF_VendorConsents"]
        
        let keys2 = ["IABTCF_VendorLegitimateInterests",
                     "IABTCF_PurposeConsents",
                     "IABTCF_PurposeLegitimateInterests",
                     "IABTCF_SpecialFeaturesOptIns",
                     "IABTCF_PublisherConsent",
                     "IABTCF_PublisherLegitimateInterests",
                     "IABTCF_PublisherCustomPurposesConsents",
                     "IABTCF_PublisherCustomPurposesLegitimateInterests",
                     "IABUSPrivacy_String"]
        
        let keys3 = ["IABGPP_HDR_Version",
                     "IABGPP_HDR_Sections",
                     "IABGPP_HDR_GppString",
                     "IABGPP_GppSID",
                     "IABGPP_tcfeuv2_GppSID"]
        
        print("\n* ----- Begin privacy strings ---- *")
        (keys + keys2 + keys3).forEach {
            print("\($0): \(UserDefaults.standard.value(forKey: $0) ?? "")")
        }
        print("* ----- End privacy strings ---- *\n")
    }
    
    private var makeParameters: [KetchUI.ExperienceOption] {
        var parameters = [KetchUI.ExperienceOption]()
        if let org = organizationText.text, !org.isEmpty {
            parameters.append(.organizationCode(org))
        }
        
        if let property = propertyText.text, !property.isEmpty {
            parameters.append(.propertyCode(property))
        }
        
        if let env = environmentText.text, !env.isEmpty {
            parameters.append(.environment(env))
        }
        
        if let lang = languageText.text, !lang.isEmpty {
            parameters.append(.language(code: lang))
        }
        
        if let jurisdiction = jurisdictionText.text, !jurisdiction.isEmpty {
            parameters.append(.jurisdiction(code: jurisdiction))
        }
        
        if let region = regionText.text, !region.isEmpty {
            parameters.append(.region(code: region))
        }
        
        parameters.append(.ketchURL(apiRegion.urlString))
        
        identities.forEach { identity in
            parameters.append(.identity(identity))
        }
        
        return parameters
    }
    
    private var apiRegion: APIRegion {
        if prodUS.isSelected {
            return APIRegion.us
        } else if prodEU.isSelected {
            return APIRegion.eu
        } else if uat.isSelected {
            return APIRegion.uat
        } else {
            return APIRegion.us // default
        }
    }
    
    private var selectedTabsNames: [String] {
        var tabNames = [String]()
        
        if overviewAllowed.isSelected {
            tabNames.append("overviewTab")
        }
        
        if consentsAllowed.isSelected {
            tabNames.append("consentsTab")
        }
        
        if subscriptionsAllowed.isSelected {
            tabNames.append("subscriptionsTab")
        }
        
        if rightsAllowed.isSelected {
            tabNames.append("rightsTab")
        }
        
        return tabNames
    }
    
    private var initialTab: String {
        if overviewInitial.isSelected {
            return "overviewTab"
        } else if consentInitial.isSelected {
            return "consentsTab"
        } else if subscriptionsInitial.isSelected {
            return "subscriptionsTab"
        } else if rightsInitial.isSelected {
            return "rightsTab"
        } else {
            return "overviewTab" // default
        }
    }
}

extension ViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
    }
}
 
enum APIRegion {
    case us, eu, uat
    
    var name: String {
        switch self {
        case .us:
            return "Prod US"
        case .eu:
            return "Prod EU"
        case .uat:
            return "UAT"
        }
    }
    
    var urlString: String {
        switch self {
        case .us:
            return "https://global.ketchcdn.com/web/v3"
        case .eu:
            return "https://eu.ketchcdn.com/web/v3"
        case .uat:
            return "https://dev.ketchcdn.com/web/v3"
        }
    }
}

// MARK: - KetchEventListener

extension ViewController: KetchEventListener {
    public func onWillShowExperience(type: KetchSDK.WillShowExperienceType) {
        print("Will show experience")
    }
    
    public func onShow() {
        print("UI Shown")
        
        guard let viewController = ketchUI.webPresentationItem?.viewController else {
            return
        }
        
        viewController.modalPresentationStyle = .overCurrentContext
        viewController.modalTransitionStyle = .crossDissolve
        present(viewController, animated: true)
    }

    public func onDismiss(status: KetchSDK.HideExperienceStatus) {
        print("UI Dismissed")
        dismiss(animated: true)
    }

    public func onEnvironmentUpdated(environment: String?) {
        print("Environment Updated: \(String(describing: environment))")
    }

    public func onRegionInfoUpdated(regionInfo: String?) {
        print("Region Info Updated: \(String(describing: regionInfo))")
    }

    public func onJurisdictionUpdated(jurisdiction: String?) {
        print("Jurisdiction Updated: \(String(describing: jurisdiction))")
    }

    public func onIdentitiesUpdated(identities: String?) {
        print("Identities Updated: \(String(describing: identities))")
    }

    public func onConsentUpdated(consent: KetchSDK.ConsentStatus) {
        print("Consent Updated: \(consent)")
    }

    public func onError(description: String) {
        print("Error: \(description)")
    }

    public func onCCPAUpdated(ccpaString: String?) {
        print("CCPA String Updated: \(String(describing: ccpaString))")
    }

    public func onTCFUpdated(tcfString: String?) {
        print("TCF String Updated: \(String(describing: tcfString))")
    }

    public func onGPPUpdated(gppString: String?) {
        print("GPP String Updated: \(String(describing: gppString))")
    }
}
