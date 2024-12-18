//
//  MyKetchViewUIKit.swift
//
//  Created by Justin Boileau on 10/12/24.
//

import UIKit
import KetchSDK

// MARK: - MyKetchUIKitEventListener
class MyKetchUIKitEventListener: KetchEventListener {
    func onLoad() {
        print("UI Loaded")
    }

    func onShow() {
        print("UI Shown")
    }

    func onDismiss(status: KetchSDK.HideExperienceStatus) {
        print("UI Dismissed")
    }

    func onEnvironmentUpdated(environment: String?) {
        print("Environment Updated: \(String(describing: environment))")
    }

    func onRegionInfoUpdated(regionInfo: String?) {
        print("Region Info Updated: \(String(describing: regionInfo))")
    }

    func onJurisdictionUpdated(jurisdiction: String?) {
        print("Jurisdiction Updated: \(String(describing: jurisdiction))")
    }

    func onIdentitiesUpdated(identities: String?) {
        print("Identities Updated: \(String(describing: identities))")
    }

    func onConsentUpdated(consent: KetchSDK.ConsentStatus) {
        print("Consent Updated: \(consent)")
    }

    func onError(description: String) {
        print("Error: \(description)")
    }

    func onCCPAUpdated(ccpaString: String?) {
        print("CCPA String Updated: \(String(describing: ccpaString))")
    }

    func onTCFUpdated(tcfString: String?) {
        print("TCF String Updated: \(String(describing: tcfString))")
    }

    func onGPPUpdated(gppString: String?) {
        print("GPP String Updated: \(String(describing: gppString))")
    }
}

// MARK: - MyKetchViewController
public class ViewController: UIViewController {
    private var ketchUI: KetchUI
    private let listener = MyKetchUIKitEventListener()
    private var selectedExperienceToShow: KetchUI.ExperienceOption.ExperienceToShow = .consent
    private var selectedTab: KetchUI.ExperienceOption.PreferencesTab?
    private var lang = "HI"
    private var jurisdiction = "australia"
    private var region = "AUS"
    private var selectedTabs = KetchUI.ExperienceOption.PreferencesTab.allCases
    
    required public init(coder: NSCoder) {
        
        let orgCode = "ketch_samples"
        let idfa = "idfa"
        
        // Initialize KetchSDK
        let ketch = KetchSDK.create(
            organizationCode: orgCode,
            propertyCode: "ios",
            environmentCode: "production",
            identities: [
                Ketch.Identity(key: "idfa", value: idfa)
            ]
        )
        
        // Initialize KetchUI
        self.ketchUI = KetchUI(
            ketch: ketch,
            experienceOptions: [
                .forceExperience(.consent)
            ]
        )
        
        super.init(nibName: nil, bundle: nil)
        
        // Set listener
        ketchUI.eventListener = listener
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    private func setupUI() {
        view.backgroundColor = .white
        
        let experienceLabel = UILabel()
        experienceLabel.text = "Experience:"
        experienceLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(experienceLabel)
        
        // Picker for Experience
        let experiencePicker = UISegmentedControl(items: ["Consent", "Preferences"])
        experiencePicker.selectedSegmentIndex = 0
        experiencePicker.translatesAutoresizingMaskIntoConstraints = false
        experiencePicker.addTarget(self, action: #selector(experiencePickerChanged), for: .valueChanged)
        view.addSubview(experiencePicker)
        
        // Language Input
        let langLabel = UILabel()
        langLabel.text = "Language:"
        langLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(langLabel)
        
        let langTextField = UITextField()
        langTextField.borderStyle = .roundedRect
        langTextField.text = lang
        langTextField.translatesAutoresizingMaskIntoConstraints = false
        langTextField.addTarget(self, action: #selector(languageChanged(_:)), for: .editingChanged)
        view.addSubview(langTextField)
       
        // Jurisdiction Input
        let jurisLabel = UILabel()
        jurisLabel.text = "Jurisdiction:"
        jurisLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(jurisLabel)
        
        let jurisTextField = UITextField()
        jurisTextField.borderStyle = .roundedRect
        jurisTextField.text = jurisdiction
        jurisTextField.translatesAutoresizingMaskIntoConstraints = false
        jurisTextField.addTarget(self, action: #selector(jurisdictionChanged(_:)), for: .editingChanged)
        view.addSubview(jurisTextField)
        
        // Jurisdiction Input
        let regionLabel = UILabel()
        regionLabel.text = "Region:"
        regionLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(regionLabel)
        
        let regionTextField = UITextField()
        regionTextField.borderStyle = .roundedRect
        regionTextField.text = region
        regionTextField.translatesAutoresizingMaskIntoConstraints = false
        regionTextField.addTarget(self, action: #selector(regionChanged(_:)), for: .editingChanged)
        view.addSubview(regionTextField)
        
        // Create the "Show" button
        let showButton = createShowButton()
        
        // Add the button to the view
        view.addSubview(showButton)
        showButton.translatesAutoresizingMaskIntoConstraints = false
        
        // Add Constraints
        NSLayoutConstraint.activate([
            experienceLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 16),
            experienceLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            
            experiencePicker.topAnchor.constraint(equalTo: experienceLabel.bottomAnchor, constant: 8),
            experiencePicker.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            experiencePicker.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),

            langLabel.topAnchor.constraint(equalTo: experiencePicker.bottomAnchor, constant: 16),
            langLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            
            langTextField.topAnchor.constraint(equalTo: langLabel.bottomAnchor, constant: 8),
            langTextField.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            langTextField.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            
            jurisLabel.topAnchor.constraint(equalTo: langTextField.bottomAnchor, constant: 16),
            jurisLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            
            jurisTextField.topAnchor.constraint(equalTo: jurisLabel.bottomAnchor, constant: 8),
            jurisTextField.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            jurisTextField.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            
            regionLabel.topAnchor.constraint(equalTo: jurisTextField.bottomAnchor, constant: 16),
            regionLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            
            regionTextField.topAnchor.constraint(equalTo: regionLabel.bottomAnchor, constant: 8),
            regionTextField.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            regionTextField.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            
            showButton.topAnchor.constraint(equalTo: regionTextField.bottomAnchor, constant: 8),
            showButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            showButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
        ])
    }
    
    @objc private func experiencePickerChanged(_ sender: UISegmentedControl) {
        selectedExperienceToShow = sender.selectedSegmentIndex == 0 ? .consent : .preferences
        print("Selected Experience: \(selectedExperienceToShow.rawValue)")
    }
    
    @objc private func languageChanged(_ sender: UITextField) {
        lang = sender.text ?? "HI"
        print("Language changed to: \(lang)")
    }
    
    @objc private func jurisdictionChanged(_ sender: UITextField) {
        jurisdiction = sender.text ?? "default"
        print("Jurisdiction changed to: \(jurisdiction)")
    }
    
    @objc private func regionChanged(_ sender: UITextField) {
        region = sender.text ?? "default"
        print("Region changed to: \(region)")
    }
    
    private func createShowButton() -> UIButton {
        // Create a UIButton instance
        let showButton = UIButton(type: .system)
        showButton.setTitle("Show", for: .normal)
        showButton.titleLabel?.font = UIFont.systemFont(ofSize: 24, weight: .bold) // Mimic `.font(.system(.title))`
        
        // Add the target-action for the button
        showButton.addTarget(self, action: #selector(showButtonTapped), for: .touchUpInside)
        
        return showButton
    }

    @objc private func showButtonTapped() {
        // Create the params array
        var params: [KetchUI.ExperienceOption?] = [
            .region(code: region),
            .language(code: lang),
            .forceExperience(selectedExperienceToShow),
            .jurisdiction(code: jurisdiction)
        ]
        
        // Add preferences-related parameters if applicable
        if !selectedTabs.isEmpty && selectedExperienceToShow == .preferences {
            let selectedTabsNames = selectedTabs.compactMap { $0.rawValue }
            params.append(.preferencesTabs(selectedTabsNames.joined(separator: ",")))
            
            if let selectedTab, selectedTabs.contains(selectedTab) {
                params.append(.preferencesTab(selectedTab))
            }
        }
        
        // Reload the KetchUI with the updated params
        ketchUI.reload(with: params.compactMap { $0 })

        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { // Delay of 2 seconds
            guard let presentationItem = self.ketchUI.webPresentationItem else {
              return
            }
            print("Presenting view controller: \(presentationItem.viewController)")
            self.present(presentationItem.viewController, animated: false) {
                print("Presentation complete")
            }
        }

    }
    
}
