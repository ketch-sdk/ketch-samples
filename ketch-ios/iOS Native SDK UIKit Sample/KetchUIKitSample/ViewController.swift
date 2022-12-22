//
//  ViewController.swift
//  KetchUIKitSample
//

import UIKit
import Combine
import KetchSDK
import AdSupport
import AppTrackingTransparency
import SwiftUI

class ViewController: UIViewController {
    @IBOutlet weak var ketchFeaturesStack: UIStackView!
    @IBOutlet weak var ketchUIFeaturesStack: UIStackView!

    private var ketch: Ketch?
    private var ketchUI: KetchUI?

    private var config: KetchSDK.Configuration?
    private var consent: KetchSDK.ConsentStatus?

    private var subscriptions = Set<AnyCancellable>()

    override func viewDidLoad() {
        super.viewDidLoad()

        ketchFeaturesStack.isHidden = true
        ketchUIFeaturesStack.isHidden = true
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        ATTrackingManager.requestTrackingAuthorization { [weak self] authorizationStatus in
            if case .authorized = authorizationStatus {
                let advertisingId = ASIdentifierManager.shared().advertisingIdentifier

                DispatchQueue.main.async {
                    self?.setupKetch(advertisingIdentifier: advertisingId)
                }
            } else if case .denied = authorizationStatus {
                let alert = UIAlertController(
                    title: "Tracking Authorization Denied by app settings",
                    message: "Please allow tracking in Settings -> Privacy -> Tracking",
                    preferredStyle: .alert
                )

                alert.addAction(
                    UIAlertAction(title: "Cancel", style: .cancel)
                )

                alert.addAction(
                    UIAlertAction(title: "Edit preferences", style: .default) { _ in
                        if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(settingsURL)
                        }
                    }
                )

                DispatchQueue.main.async { [weak self] in
                    self?.present(alert, animated: true)
                }
            }
        }
    }

    @IBAction func showDialogsAutomaticallyChanged(_ sender: UISwitch) {
        ketchUI?.showDialogsIfNeeded = sender.isOn
    }

    @IBAction func configurationAction() {
        ketch?.loadConfiguration()
    }

    enum Jurisdiction {
        static let GDPR = "gdpr"
        static let CCPA = "ccpa"
    }

    @IBAction func configurationGDPRAction() {
        ketch?.loadConfiguration(jurisdiction: Jurisdiction.GDPR)
    }

    @IBAction func configurationCCPAAction() {
        ketch?.loadConfiguration(jurisdiction: Jurisdiction.CCPA)
    }

    @IBAction func invokeRightsAction() {
        guard let ketch, let config = ketch.configuration else { return }

        let user = KetchSDK.InvokeRightConfig.User(
            email: "user@email.com",
            first: "FirstName",
            last: "LastName",
            country: nil,
            stateRegion: nil,
            description: nil,
            phone: nil,
            postalCode: nil,
            addressLine1: nil,
            addressLine2: nil
        )

        ketch.invokeRights(right: config.rights?.first, user: user)
    }

    @IBAction func getConsentAction() {
        guard let ketch else { return }

        ketch.loadConsent()
    }

    @IBAction func updateConsentAction() {
        guard let ketch, let config = ketch.configuration else { return }

        let purposes = config.purposes?
            .reduce(into: [String: KetchSDK.ConsentUpdate.PurposeAllowedLegalBasis]()) { result, purpose in
                result[purpose.code] = .init(allowed: true, legalBasisCode: purpose.legalBasisCode)
            }

        let vendors = config.vendors?.map(\.id)

        ketch.updateConsent(purposes: purposes, vendors: vendors)
    }

    @IBAction func showBannerAction() {
        ketchUI?.showBanner()
    }

    @IBAction func showModalAction() {
        ketchUI?.showModal()
    }

    @IBAction func showPreferenceAction() {
        ketchUI?.showPreference()
    }

    @IBAction func showJitAction() {
        guard let ketchUI, let purpose = ketchUI.configuration?.purposes?.first else { return }

        ketchUI.showJIT(purpose: purpose)
    }

    private func setupKetch(advertisingIdentifier: UUID) {
        let ketch = KetchSDK.create(
            organizationCode: <#Your Organization Code#>,
            propertyCode: <#Your Property ID#>,
            environmentCode: <#Your Environment ID#>,
            controllerCode: <#Your Controller Code#>,
            identities: [.idfa(advertisingIdentifier.uuidString)]
        )

        ketch.add(plugins: [TCF(), CCPA()])

        self.ketch = ketch
        ketchUI = KetchUI(ketch: ketch)

        bindInput()
    }

    private func bindInput() {
        ketch?.configurationPublisher
            .receive(on: DispatchQueue.main)
            .replaceError(with: nil)
            .sink { config in
                self.config = config

                self.ketchFeaturesStack.isHidden = config == nil
            }
            .store(in: &subscriptions)

        ketch?.consentPublisher
            .receive(on: DispatchQueue.main)
            .replaceError(with: nil)
            .sink { consent in
                self.consent = consent

                self.ketchUIFeaturesStack.isHidden = consent == nil
            }
            .store(in: &subscriptions)

        ketchUI?.$presentationItem
            .receive(on: DispatchQueue.main)
            .sink { presentationItem in
                guard let presentationItem else { return }

                self.present(presentationItem.viewController, animated: true)
            }
            .store(in: &subscriptions)
    }
}
