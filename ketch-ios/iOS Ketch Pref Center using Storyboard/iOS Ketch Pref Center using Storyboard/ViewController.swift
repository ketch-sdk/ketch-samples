//
//  ViewController.swift
//  iOS Ketch Pref Center using Storyboard
//

import UIKit
import AdSupport
import AppTrackingTransparency

private var advertisingId: UUID?

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
                advertisingId = ASIdentifierManager.shared().advertisingIdentifier

                DispatchQueue.main.async { [weak self] in
                    self?.showConsent()
                }
            }
        }
    }

    private func showConsent() {
        let vc = ConsentViewController(
            config: .init(
                propertyName: "website_smart_tag",
                orgCode: "transcenda",
                identities: [.advertisingIdentifier(advertisingId!)]
            )
        )

        let navVC = UINavigationController(rootViewController: vc)
        self.present(navVC, animated: true)
    }
}

