//
//  ConsentViewController.swift
//  iOS Ketch Pref Center using Storyboard
//

import UIKit
import WebKit

class ConsentViewController: UIViewController {
    let config: ConsentConfig
    lazy var webView: WKWebView = config.preferencesWebView(
        onClose: {
            self.dismiss(animated: true)
        }
    )

    init(config: ConsentConfig) {
        self.config = config
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        if webView.superview == nil {
            view.addSubview(webView)
            webView.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
                webView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
                webView.topAnchor.constraint(equalTo: view.topAnchor),
                webView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
            ])
        }
    }
}
