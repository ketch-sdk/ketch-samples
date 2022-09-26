//
//  ViewController.swift
//  iOS Ketch Pref Center using Storyboard
//

import UIKit
import AdSupport
import AppTrackingTransparency

class ViewController: UIViewController {

    private let button: UIButton = {
       let button = UIButton()
        button.setTitle("Preference Center", for: .normal)
        button.backgroundColor = .link
        button.setTitleColor(.white, for: .normal)
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.addSubview(button)
        button.addTarget(self, action: #selector(didTapButton), for: .touchUpInside)
        button.frame = CGRect(x: 0, y: 0, width: 220, height: 50)
        button.center = view.center
    }
    
    @objc private func didTapButton() {
        ATTrackingManager.requestTrackingAuthorization { authorizationStatus in
            if case .authorized = authorizationStatus {
                let advertisingId = ASIdentifierManager.shared().advertisingIdentifier

                DispatchQueue.main.async { [weak self] in
                    self?.showConsent(advertisingId: advertisingId)
                }
            } else if case .denied = authorizationStatus {
                let alert = UIAlertController(
                    title: "Tracking Authorization Denied by app settings",
                    message: "Please allow tracking in Settings -> Privacy -> Tracking",
                    preferredStyle: .alert
                )

                alert.addAction(
                    UIAlertAction(
                        title: "Cancel",
                        style: .cancel
                    )
                )

                alert.addAction(
                    UIAlertAction(
                        title: "Edit preferences",
                        style: .default,
                        handler: { _ in
                            if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
                                UIApplication.shared.open(settingsURL)
                            }
                        }
                    )
                )

                DispatchQueue.main.async { [weak self] in
                    self?.present(alert, animated: true)
                }
            }
        }
    }

    private func showConsent(advertisingId: UUID) {
        let vc = ConsentViewController(
            config: .init(
                propertyName: "website_smart_tag",
                orgCode: "transcenda",
                advertisingIdentifier: advertisingId
            )
        )

        let navVC = UINavigationController(rootViewController: vc)
        present(navVC, animated: true)
    }
}
